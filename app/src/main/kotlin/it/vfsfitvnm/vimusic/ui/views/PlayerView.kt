package it.vfsfitvnm.vimusic.ui.views

import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.media.audiofx.AudioEffect
import android.text.format.DateUtils
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.BlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.DarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberError
import it.vfsfitvnm.vimusic.utils.rememberMediaItem
import it.vfsfitvnm.vimusic.utils.rememberMediaItemIndex
import it.vfsfitvnm.vimusic.utils.rememberPositionAndDuration
import it.vfsfitvnm.vimusic.utils.rememberRepeatMode
import it.vfsfitvnm.vimusic.utils.rememberShouldBePlaying
import it.vfsfitvnm.vimusic.utils.rememberVolume
import it.vfsfitvnm.vimusic.utils.seamlessPlay
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.verticalFadingEdge
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@ExperimentalAnimationApi
@Composable
fun PlayerView(
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val menuState = LocalMenuState.current

    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    binder?.player ?: return

    val nullableMediaItem by rememberMediaItem(binder.player)

    val mediaItem = nullableMediaItem ?: return

    val shouldBePlaying by rememberShouldBePlaying(binder.player)
    val positionAndDuration by rememberPositionAndDuration(binder.player)

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        onSwiped = binder.player::clearMediaItems,
        collapsedContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colorPalette.elevatedBackground)
                    .fillMaxSize()
                    .drawBehind {
                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue
                        val offset = Dimensions.thumbnails.player.songPreview.toPx()

                        drawLine(
                            color = colorPalette.text,
                            start = Offset(
                                x = offset,
                                y = 1.dp.toPx()
                            ),
                            end = Offset(
                                x = ((size.width - offset) * progress) + offset,
                                y = 1.dp.toPx()
                            ),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.player.songPreview.px),
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
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        style = typography.xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (shouldBePlaying) {
                    Image(
                        painter = painterResource(R.drawable.pause),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = binder.player::pause)
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                            .size(22.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                }
                                binder.player.play()
                            }
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                            .size(22.dp)
                    )
                }
            }
        }
    ) {
        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .background(colorPalette.background)
                        .padding(top = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.66f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Thumbnail(
                            isShowingLyrics = isShowingLyrics,
                            onShowLyrics = { isShowingLyrics = it },
                            isShowingStatsForNerds = isShowingStatsForNerds,
                            onShowStatsForNerds = { isShowingStatsForNerds = it },
                            nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                        )
                    }

                    Controls(
                        mediaItem = mediaItem,
                        shouldBePlaying = shouldBePlaying,
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .background(colorPalette.background)
                        .padding(top = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1.25f)
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                    ) {
                        Thumbnail(
                            isShowingLyrics = isShowingLyrics,
                            onShowLyrics = { isShowingLyrics = it },
                            isShowingStatsForNerds = isShowingStatsForNerds,
                            onShowStatsForNerds = { isShowingStatsForNerds = it },
                            nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                        )
                    }

                    Controls(
                        mediaItem = mediaItem,
                        shouldBePlaying = shouldBePlaying,
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        PlayerBottomSheet(
            layoutState = rememberBottomSheetState(64.dp, layoutState.upperBound),
            isShowingLyrics = isShowingLyrics,
            onShowLyrics = {
                isShowingStatsForNerds = false
                isShowingLyrics = !isShowingLyrics
            },
            isShowingStatsForNerds = isShowingStatsForNerds,
            onShowStatsForNerds = {
                isShowingLyrics = false
                isShowingStatsForNerds = !isShowingStatsForNerds
            },
            onShowMenu = {
                menuState.display {
                    val resultRegistryOwner = LocalActivityResultRegistryOwner.current

                    BaseMediaItemMenu(
                        mediaItem = mediaItem,
                        onStartRadio = {
                            binder.stopRadio()
                            binder.player.seamlessPlay(mediaItem)
                            binder.setupRadio(
                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                            )
                        },
                        onGoToEqualizer = {
                            val intent =
                                Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                    putExtra(
                                        AudioEffect.EXTRA_AUDIO_SESSION,
                                        binder.player.audioSessionId
                                    )
                                    putExtra(
                                        AudioEffect.EXTRA_PACKAGE_NAME,
                                        context.packageName
                                    )
                                    putExtra(
                                        AudioEffect.EXTRA_CONTENT_TYPE,
                                        AudioEffect.CONTENT_TYPE_MUSIC
                                    )
                                }

                            if (intent.resolveActivity(context.packageManager) != null) {
                                val contract =
                                    ActivityResultContracts.StartActivityForResult()

                                resultRegistryOwner?.activityResultRegistry
                                    ?.register("", contract) {}
                                    ?.launch(intent)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "No equalizer app found!",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        },
                        onSetSleepTimer = {},
                        onDismiss = menuState::hide,
                        onGlobalRouteEmitted = layoutState::collapseSoft,
                    )
                }
            },
            onGlobalRouteEmitted = layoutState::collapseSoft,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun Thumbnail(
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val mediaItemIndex by rememberMediaItemIndex(player)

    val error by rememberError(player)

    if (error == null) {
        AnimatedContent(
            targetState = mediaItemIndex,
            transitionSpec = {
                val slideDirection =
                    if (targetState > initialState) AnimatedContentScope.SlideDirection.Left else AnimatedContentScope.SlideDirection.Right

                (slideIntoContainer(slideDirection) + fadeIn() with
                        slideOutOfContainer(slideDirection) + fadeOut()).using(
                    SizeTransform(clip = false)
                )
            },
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1f)
        ) { currentMediaItemIndex ->
            val mediaItem = remember(currentMediaItemIndex) {
                player.getMediaItemAt(currentMediaItemIndex)
            }

            Box(
                modifier = Modifier
                    .clip(ThumbnailRoundness.shape)
                    .size(thumbnailSizeDp)
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onShowLyrics(true)
                                },
                                onLongPress = {
                                    onShowStatsForNerds(true)
                                }
                            )
                        }
                        .fillMaxSize()
                )

                Lyrics(
                    mediaId = mediaItem.mediaId,
                    isDisplayed = isShowingLyrics,
                    onDismiss = {
                        onShowLyrics(false)
                    },
                    onLyricsUpdate = { mediaId, lyrics ->
                        if (Database.updateLyrics(mediaId, lyrics) == 0) {
                            if (mediaId == mediaItem.mediaId) {
                                Database.insert(mediaItem) { song ->
                                    song.copy(lyrics = lyrics)
                                }
                            }
                        }
                    },
                    size = thumbnailSizeDp,
                    mediaMetadataProvider = mediaItem::mediaMetadata,
                    nestedScrollConnectionProvider = nestedScrollConnectionProvider,
                )

                StatsForNerds(
                    mediaId = mediaItem.mediaId,
                    isDisplayed = isShowingStatsForNerds,
                    onDismiss = {
                        onShowStatsForNerds(false)
                    },
                    modifier = Modifier
                )
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .padding(bottom = 32.dp)
                .padding(horizontal = 32.dp)
                .size(thumbnailSizeDp)
        ) {
            LoadingOrError(
                errorMessage = error?.javaClass?.canonicalName,
                onRetry = {
                    player.playWhenReady = true
                    player.prepare()
                }
            ) {}
        }
    }
}

@Composable
private fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    onLyricsUpdate: (String, String) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
    modifier: Modifier = Modifier
) {
    val (_, typography) = LocalAppearance.current
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var isLoading by remember(mediaId) {
            mutableStateOf(false)
        }

        var isEditingLyrics by remember(mediaId) {
            mutableStateOf(false)
        }

        val lyrics by remember(mediaId) {
            Database.lyrics(mediaId).distinctUntilChanged().map flowMap@{ lyrics ->
                if (lyrics != null) return@flowMap lyrics

                isLoading = true

                YouTube.next(mediaId, null)?.map { nextResult ->
                    nextResult.lyrics?.text()?.map { newLyrics ->
                        onLyricsUpdate(mediaId, newLyrics ?: "")
                        isLoading = false
                        return@flowMap newLyrics ?: ""
                    }
                }

                isLoading = false
                null
            }.distinctUntilChanged()
        }.collectAsState(initial = ".", context = Dispatchers.IO)

        if (isEditingLyrics) {
            TextFieldDialog(
                hintText = "Enter the lyrics",
                initialTextInput = lyrics ?: "",
                singleLine = false,
                maxLines = 10,
                isTextInputValid = { true },
                onDismiss = {
                    isEditingLyrics = false
                },
                onDone = {
                    query {
                        Database.updateLyrics(mediaId, it)
                    }
                }
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .fillMaxSize()
                .background(Color.Black.copy(0.8f))
        ) {
            AnimatedVisibility(
                visible = !isLoading && lyrics == null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "An error has occurred while fetching the lyrics",
                    style = typography.xs.center.medium.color(BlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = lyrics?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "Lyrics are not available for this song",
                    style = typography.xs.center.medium.color(BlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(4) { index ->
                        TextPlaceholder(
                            modifier = Modifier
                                .alpha(1f - index * 0.05f)
                        )
                    }
                }
            } else {
                lyrics?.let { lyrics ->
                    if (lyrics.isNotEmpty()) {
                        BasicText(
                            text = lyrics,
                            style = typography.xs.center.medium.color(BlackColorPalette.text),
                            modifier = Modifier
                                .nestedScroll(remember { nestedScrollConnectionProvider() })
                                .verticalFadingEdge()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = size / 4, horizontal = 32.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .size(size)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(DarkColorPalette.text),
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .clickable {
                                    val mediaMetadata = mediaMetadataProvider()

                                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                        putExtra(
                                            SearchManager.QUERY,
                                            "${mediaMetadata.title} ${mediaMetadata.artist} lyrics"
                                        )
                                    }

                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "No browser app found!",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                                .padding(all = 8.dp)
                                .size(20.dp)
                        )

                        Image(
                            painter = painterResource(R.drawable.pencil),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(DarkColorPalette.text),
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .clickable {
                                    isEditingLyrics = true
                                }
                                .padding(all = 8.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsForNerds(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (_, typography) = LocalAppearance.current
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current ?: return

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var cachedBytes by remember(mediaId) {
            mutableStateOf(binder.cache.getCachedBytes(mediaId, 0, -1))
        }

        val format by remember(mediaId) {
            Database.format(mediaId).distinctUntilChanged()
        }.collectAsState(initial = null, context = Dispatchers.IO)

        val volume by rememberVolume(binder.player)

        DisposableEffect(mediaId) {
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

            binder.cache.addListener(mediaId, listener)

            onDispose {
                binder.cache.removeListener(mediaId, listener)
            }
        }

        Box(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .background(Color.Black.copy(alpha = 0.8f))
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    BasicText(
                        text = "Id",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "Volume",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "Loudness",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "Bitrate",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "Size",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "Cached",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                }

                Column {
                    BasicText(
                        text = mediaId,
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = "${volume.times(100).roundToInt()}%",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = format?.loudnessDb?.let { loudnessDb ->
                            "%.2f dB".format(loudnessDb)
                        } ?: "Unknown",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = format?.bitrate?.let { bitrate ->
                            "${bitrate / 1000} kbps"
                        } ?: "Unknown",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = format?.contentLength?.let { contentLength ->
                            Formatter.formatShortFileSize(
                                context,
                                contentLength
                            )
                        } ?: "Unknown",
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                    BasicText(
                        text = buildString {
                            append(Formatter.formatShortFileSize(context, cachedBytes))

                            format?.contentLength?.let { contentLength ->
                                append(" (${(cachedBytes.toFloat() / contentLength * 100).roundToInt()}%)")
                            }
                        },
                        style = typography.xs.medium.color(BlackColorPalette.text)
                    )
                }
            }

            if (format != null && format?.itag == null) {
                BasicText(
                    text = "FETCH MISSING DATA",
                    style = typography.xxs.medium.color(BlackColorPalette.text),
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                query {
                                    runBlocking(Dispatchers.IO) {
                                        YouTube
                                            .player(mediaId)
                                            ?.map { response ->
                                                response.streamingData?.adaptiveFormats
                                                    ?.findLast { format ->
                                                        format.itag == 251 || format.itag == 140
                                                    }
                                                    ?.let { format ->
                                                        it.vfsfitvnm.vimusic.models.Format(
                                                            songId = mediaId,
                                                            itag = format.itag,
                                                            mimeType = format.mimeType,
                                                            bitrate = format.bitrate,
                                                            loudnessDb = response.playerConfig?.audioConfig?.loudnessDb?.toFloat(),
                                                            contentLength = format.contentLength,
                                                            lastModified = format.lastModified
                                                        )
                                                    }
                                            }
                                    }
                                        ?.getOrNull()
                                        ?.let(Database::insert)
                                }
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun Controls(
    mediaItem: MediaItem,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val repeatMode by rememberRepeatMode(binder.player)

    var scrubbingPosition by remember(mediaItem.mediaId) {
        mutableStateOf<Long?>(null)
    }

    val likedAt by remember(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged()
    }.collectAsState(initial = null, context = Dispatchers.IO)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        BasicText(
            text = mediaItem.mediaMetadata.title?.toString() ?: "",
            style = typography.l.bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        BasicText(
            text = mediaItem.mediaMetadata.artist?.toString() ?: "",
            style = typography.s.semiBold.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier
                .weight(0.5f)
        )

        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = colorPalette.text,
            backgroundColor = colorPalette.textDisabled,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicText(
                text = DateUtils.formatElapsedTime((scrubbingPosition ?: position) / 1000),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                BasicText(
                    text = DateUtils.formatElapsedTime(duration / 1000),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }


        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.heart),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (likedAt != null) colorPalette.red else colorPalette.textDisabled),
                modifier = Modifier
                    .clickable {
                        query {
                            if (Database.like(
                                    mediaItem.mediaId,
                                    if (likedAt == null) System.currentTimeMillis() else null
                                ) == 0
                            ) {
                                Database.insert(mediaItem, Song::toggleLike)
                            }
                        }
                    }
                    .weight(1f)
                    .size(28.dp)
            )

            Image(
                painter = painterResource(R.drawable.play_skip_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(onClick = binder.player::seekToPrevious)
                    .weight(1f)
                    .size(28.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Box(
                modifier = Modifier
                    .clickable {
                        if (shouldBePlaying) {
                            binder.player.pause()
                        } else {
                            if (binder.player.playbackState == Player.STATE_IDLE) {
                                binder.player.prepare()
                            }
                            binder.player.play()
                        }
                    }
                    .background(color = colorPalette.text, shape = CircleShape)
                    .size(64.dp)
            ) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.background),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Image(
                painter = painterResource(R.drawable.play_skip_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(onClick = binder.player::seekToNext)
                    .weight(1f)
                    .size(28.dp)
            )

            Image(
                painter = painterResource(
                    if (repeatMode == Player.REPEAT_MODE_ONE) {
                        R.drawable.repeat_one
                    } else {
                        R.drawable.repeat
                    }
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    if (repeatMode == Player.REPEAT_MODE_OFF) {
                        colorPalette.textDisabled
                    } else {
                        colorPalette.text
                    }
                ),
                modifier = Modifier
                    .clickable {
                        binder.player.repeatMode
                            .plus(2)
                            .mod(3)
                            .let {
                                binder.player.repeatMode = it
                            }
                    }
                    .weight(1f)
                    .size(28.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )
    }
}
