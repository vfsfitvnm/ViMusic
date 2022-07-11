package it.vfsfitvnm.vimusic.ui.views

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.*
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.*
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.PlayerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@ExperimentalAnimationApi
@Composable
fun PlayerView(
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val menuState = LocalMenuState.current
    val preferences = LocalPreferences.current
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    val player = binder?.player
    val playerState = rememberPlayerState(player)

    player ?: return
    playerState?.mediaItem ?: return

    val coroutineScope = rememberCoroutineScope()

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {
            if (!layoutState.isExpanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(layoutState.lowerBound)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = 1f - (layoutState.progress * 16).coerceAtMost(1f)
                        }
                        .background(colorPalette.elevatedBackground)
                        .drawBehind {
                            val offset = Dimensions.thumbnails.player.songPreview.toPx()

                            drawLine(
                                color = colorPalette.text,
                                start = Offset(
                                    x = offset,
                                    y = 1.dp.toPx()
                                ),
                                end = Offset(
                                    x = ((size.width - offset) * playerState.progress) + offset,
                                    y = 1.dp.toPx()
                                ),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                ) {
                    AsyncImage(
                        model = playerState.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.player.songPreview.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(Dimensions.thumbnails.player.songPreview)
                    )

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        BasicText(
                            text = playerState.mediaMetadata.title?.toString() ?: "",
                            style = typography.xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        BasicText(
                            text = playerState.mediaMetadata.artist?.toString() ?: "",
                            style = typography.xs.semiBold.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    when {
                        playerState.playbackState == Player.STATE_ENDED || !playerState.playWhenReady -> Image(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    if (playerState.playbackState == Player.STATE_IDLE) {
                                        player.prepare()
                                    }
                                    player.play()
                                }
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )
                        else -> Image(
                            painter = painterResource(R.drawable.pause),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable(onClick = player::pause)
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        val song by remember(playerState.mediaItem.mediaId) {
            playerState.mediaItem.mediaId.let(Database::song).distinctUntilChanged()
        }.collectAsState(initial = null, context = Dispatchers.IO)

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(colorPalette.background)
                .padding(bottom = 72.dp)
                .fillMaxSize()
        ) {
            var scrubbingPosition by remember(playerState.mediaItemIndex) {
                mutableStateOf<Long?>(null)
            }

            TopAppBar {
                Spacer(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(24.dp)
                )

                Image(
                    painter = painterResource(R.drawable.ellipsis_horizontal),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable {
                            menuState.display {
                                QueuedMediaItemMenu(
                                    mediaItem = playerState.mediaItem,
                                    indexInQueue = null,
                                    onDismiss = menuState::hide,
                                    onGlobalRouteEmitted = layoutState.collapse
                                )
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(24.dp)
                )
            }

            if (playerState.error == null) {
                AnimatedContent(
                    targetState = playerState.mediaItemIndex,
                    transitionSpec = {
                        val slideDirection =
                            if (targetState > initialState) AnimatedContentScope.SlideDirection.Left else AnimatedContentScope.SlideDirection.Right

                        (slideIntoContainer(slideDirection) + fadeIn() with
                                slideOutOfContainer(slideDirection) + fadeOut()).using(
                            SizeTransform(clip = false)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val artworkUri = remember(it) {
                        player.getMediaItemAt(it).mediaMetadata.artworkUri.thumbnail(
                            thumbnailSizePx
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .padding(horizontal = 32.dp)
                            .aspectRatio(1f)
                            .clip(ThumbnailRoundness.shape)
                            .size(thumbnailSizeDp)
                    ) {
                        AsyncImage(
                            model = artworkUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            isShowingStatsForNerds = true
                                        }
                                    )
                                }
                                .fillMaxSize()
                        )

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isShowingStatsForNerds,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            var cachedBytes by remember(song?.id) {
                                mutableStateOf(binder.cache.getCachedBytes(playerState.mediaItem.mediaId, 0, -1))
                            }

                            val loudnessDb by remember {
                                derivedStateOf {
                                    song?.loudnessDb ?: playerState.mediaMetadata.extras?.getFloatOrNull("loudnessDb")
                                }
                            }

                            val contentLength by remember {
                                derivedStateOf {
                                    song?.contentLength ?: playerState.mediaMetadata.extras?.getLongOrNull("contentLength")
                                }
                            }

                            DisposableEffect(song?.id) {
                                val key = playerState.mediaItem.mediaId

                                val listener = object : Cache.Listener {
                                    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
                                        cachedBytes += span.length
                                    }

                                    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
                                        cachedBytes -= span.length
                                    }

                                    override fun onSpanTouched(
                                        cache: Cache,
                                        oldSpan: CacheSpan,
                                        newSpan: CacheSpan
                                    ) = Unit
                                }

                                binder.cache.addListener(key, listener)

                                onDispose {
                                    binder.cache.removeListener(key, listener)
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isShowingStatsForNerds = false
                                            }
                                        )
                                    }
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .fillMaxSize()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .padding(all = 16.dp)
                                ) {
                                    Column {
                                        BasicText(
                                            text = "Id",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = "Volume",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = "Loudness",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = "Size",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = "Cached",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                    }

                                    Column {
                                        BasicText(
                                            text = playerState.mediaItem.mediaId,
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = "${playerState.volume.times(100).roundToInt()}%",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = loudnessDb?.let { loudnessDb ->
                                                "%.2f dB".format(loudnessDb)
                                            } ?: "Unknown",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = contentLength?.let { contentLength ->
                                                Formatter.formatShortFileSize(
                                                    context,
                                                    contentLength
                                                )
                                            } ?: "Unknown",
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                        BasicText(
                                            text = buildString {
                                                append(Formatter.formatShortFileSize(context, cachedBytes))

                                                contentLength?.let { contentLength ->
                                                    append(" (${(cachedBytes.toFloat() / contentLength * 100).roundToInt()}%)")
                                                }
                                            },
                                            style = typography.xs.semiBold.color(BlackColorPalette.text)
                                        )
                                    }
                                }

                                if (song != null && (contentLength == null || loudnessDb == null)) {
                                    BasicText(
                                        text = "FILL MISSING DATA",
                                        style = typography.xxs.semiBold.color(BlackColorPalette.text),
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = {
                                                    song?.let { song ->
                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            YouTube
                                                                .player(song.id)
                                                                ?.map { body ->
                                                                    Database.update(
                                                                        song.copy(
                                                                            loudnessDb = body.playerConfig?.audioConfig?.loudnessDb?.toFloat(),
                                                                            contentLength = body.streamingData?.adaptiveFormats
                                                                                ?.findLast { format ->
                                                                                    format.itag == 251
                                                                                }
                                                                                ?.let(PlayerResponse.StreamingData.AdaptiveFormat::contentLength)
                                                                        )
                                                                    )
                                                                }
                                                        }
                                                    }
                                                }
                                            )
                                            .padding(all = 16.dp)
                                            .align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 32.dp)
                        .size(thumbnailSizeDp)
                ) {
                    LoadingOrError(
                        errorMessage = playerState.error.javaClass.canonicalName,
                        onRetry = {
                            player.playWhenReady = true
                            player.prepare()
                        }
                    ) {}
                }
            }

            BasicText(
                text = playerState.mediaMetadata.title?.toString() ?: "",
                style = typography.l.bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
            )

            BasicText(
                text = playerState.mediaMetadata.artist?.toString() ?: "",
                style = typography.s.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
            )

            SeekBar(
                value = scrubbingPosition ?: playerState.currentPosition,
                minimumValue = 0,
                maximumValue = playerState.duration,
                onDragStart = {
                    scrubbingPosition = it
                },
                onDrag = { delta ->
                    scrubbingPosition = if (playerState.duration != C.TIME_UNSET) {
                        scrubbingPosition?.plus(delta)?.coerceIn(0, playerState.duration)
                    } else {
                        null
                    }
                },
                onDragEnd = {
                    scrubbingPosition?.let(player::seekTo)
                    scrubbingPosition = null
                },
                color = colorPalette.text,
                backgroundColor = colorPalette.textDisabled,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 12.dp)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                BasicText(
                    text = DateUtils.formatElapsedTime(
                        (scrubbingPosition ?: playerState.currentPosition) / 1000
                    ),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (playerState.duration != C.TIME_UNSET) {
                    BasicText(
                        text = DateUtils.formatElapsedTime(playerState.duration / 1000),
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 32.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        song?.likedAt?.let { colorPalette.red } ?: colorPalette.textDisabled
                    ),
                    modifier = Modifier
                        .clickable {
                            query {
                                song?.let { song ->
                                    Database.update(song.toggleLike())
                                } ?: Database.insert(playerState.mediaItem, Song::toggleLike)
                            }
                        }
                        .padding(horizontal = 16.dp)
                        .size(28.dp)
                )

                Image(
                    painter = painterResource(R.drawable.play_skip_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable(onClick = player::seekToPrevious)
                        .padding(horizontal = 16.dp)
                        .size(28.dp)
                )

                val isPaused = playerState.playbackState == Player.STATE_ENDED || !playerState.playWhenReady

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable {
                            if (isPaused) {
                                if (player.playbackState == Player.STATE_IDLE) {
                                    player.prepare()
                                }

                                player.play()
                            } else {
                                player.pause()
                            }
                        }
                        .background(color = colorPalette.text, shape = CircleShape)
                        .size(64.dp)
                ) {
                    Image(
                        painter = painterResource(if (isPaused) R.drawable.play else R.drawable.pause),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.background),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp)
                    )
                }

                Image(
                    painter = painterResource(R.drawable.play_skip_forward),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable(onClick = player::seekToNext)
                        .padding(horizontal = 16.dp)
                        .size(28.dp)
                )

                Image(
                    painter = painterResource(
                        if (playerState.repeatMode == Player.REPEAT_MODE_ONE) {
                            R.drawable.repeat_one
                        } else {
                            R.drawable.repeat
                        }
                    ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (playerState.repeatMode == Player.REPEAT_MODE_OFF) {
                            colorPalette.textDisabled
                        } else {
                            colorPalette.text
                        }
                    ),
                    modifier = Modifier
                        .clickable {
                            player.repeatMode
                                .plus(2)
                                .mod(3)
                                .let { repeatMode ->
                                    player.repeatMode = repeatMode
                                    preferences.repeatMode = repeatMode
                                }
                        }
                        .padding(horizontal = 16.dp)
                        .size(28.dp)
                )
            }
        }

        PlayerBottomSheet(
            playerState = playerState,
            layoutState = rememberBottomSheetState(64.dp, layoutState.upperBound - Dimensions.playerBottomSheetPeekHeight),
            onGlobalRouteEmitted = layoutState.collapse,
            song = song,
            modifier = Modifier
                .padding(bottom = Dimensions.playerBottomSheetPeekHeight)
                .align(Alignment.BottomCenter)
        )
    }
}

