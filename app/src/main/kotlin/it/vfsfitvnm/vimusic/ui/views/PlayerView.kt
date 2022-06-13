package it.vfsfitvnm.vimusic.ui.views

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.*
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


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
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val player = LocalYoutubePlayer.current

    val coroutineScope = rememberCoroutineScope()

    player?.mediaItem ?: return

    val smallThumbnailSize = remember {
        density.run { 64.dp.roundToPx() }
    }

    val (thumbnailSizeDp, thumbnailSizePx) = remember {
        val size = minOf(configuration.screenHeightDp, configuration.screenWidthDp).dp
        size to density.run { size.minus(64.dp).roundToPx() }
    }

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {
            if (!layoutState.isExpanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(layoutState.lowerBound)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = 1f - (layoutState.progress * 16).coerceAtMost(1f)
                        }
                        .background(colorPalette.elevatedBackground)
                        .drawBehind {
                            val offset = 64.dp.toPx()

                            drawLine(
                                color = colorPalette.text,
                                start = Offset(
                                    x = offset,
                                    y = 1.dp.toPx()
                                ),
                                end = Offset(
                                    x = ((size.width - offset) * player.progress) + offset,
                                    y = 1.dp.toPx()
                                ),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                ) {
                    AsyncImage(
                        model = "${player.mediaMetadata.artworkUri}-w$smallThumbnailSize-h$smallThumbnailSize",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        BasicText(
                            text = player.mediaMetadata.title?.toString() ?: "",
                            style = typography.xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        BasicText(
                            text = player.mediaMetadata.artist?.toString() ?: "",
                            style = typography.xs,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    when {
                        player.playbackState == Player.STATE_ENDED || !player.playWhenReady -> Image(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    if (player.playbackState == Player.STATE_IDLE) {
                                        player.mediaController.prepare()
                                    }
                                    player.mediaController.play()
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
                                .clickable {
                                    player.mediaController.pause()
                                }
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        val song by remember(player.mediaItem?.mediaId) {
            player.mediaItem?.mediaId?.let(Database::songFlow)?.distinctUntilChanged() ?: flowOf(null)
        }.collectAsState(initial = null, context = Dispatchers.IO)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(colorPalette.background)
                .padding(bottom = 72.dp)
                .fillMaxSize()
        ) {
            var scrubbingPosition by remember(player.mediaItemIndex) {
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
                                    mediaItem = player.mediaItem ?: MediaItem.EMPTY,
                                    indexInQueue = player.mediaItemIndex,
                                    onDismiss = menuState::hide,
                                    onGlobalRouteEmitted = layoutState.collapse
                                )
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(24.dp)
                )
            }

            if (player.error == null) {
                AnimatedContent(
                    targetState = player.mediaItemIndex,
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
                        player.mediaController.getMediaItemAt(it).mediaMetadata.artworkUri
                    }

                    AsyncImage(
                        model = "$artworkUri-w$thumbnailSizePx-h$thumbnailSizePx",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .padding(horizontal = 32.dp)
                            .aspectRatio(1f)
                            .clip(ThumbnailRoundness.shape)
                            .size(thumbnailSizeDp)
                    )
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
                    Error(
                        error = Outcome.Error.Unhandled(player.error!!),
                        onRetry = {
                            player.mediaController.playWhenReady = true
                            player.mediaController.prepare()
                            player.error = null
                        }
                    )
                }
            }

            BasicText(
                text = player.mediaMetadata.title?.toString() ?: "",
                style = typography.l.bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
            )

            BasicText(
                text = player.mediaMetadata.extras?.getStringArrayList("artistNames")
                    ?.joinToString("") ?: "",
                style = typography.s.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
            )

            SeekBar(
                value = scrubbingPosition ?: player.currentPosition,
                minimumValue = 0,
                maximumValue = player.duration,
                onDragStart = {
                    scrubbingPosition = it
                },
                onDrag = { delta ->
                    scrubbingPosition = if (player.duration != C.TIME_UNSET) {
                        scrubbingPosition?.plus(delta)?.coerceIn(0, player.duration)
                    } else {
                        null
                    }
                },
                onDragEnd = {
                    player.mediaController.seekTo(scrubbingPosition ?: player.mediaController.currentPosition)
                    player.currentPosition = player.mediaController.currentPosition
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
                    text = DateUtils.formatElapsedTime((scrubbingPosition ?: player.currentPosition) / 1000),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (player.duration != C.TIME_UNSET) {
                    BasicText(
                        text = DateUtils.formatElapsedTime(player.duration / 1000),
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
                            coroutineScope.launch(Dispatchers.IO) {
                                Database.update(
                                    (song ?: Database.insert(player.mediaItem!!)).toggleLike()
                                )
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
                        .clickable {
                            player.mediaController.seekToPrevious()
                        }
                        .padding(horizontal = 16.dp)
                        .size(32.dp)
                )

                when {
                    player.playbackState == Player.STATE_ENDED || !player.playWhenReady -> Image(
                        painter = painterResource(R.drawable.play_circle),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable {
                                if (player.playbackState == Player.STATE_IDLE) {
                                    player.mediaController.prepare()
                                }

                                player.mediaController.play()
                            }
                            .size(64.dp)
                    )
                    else -> Image(
                        painter = painterResource(R.drawable.pause_circle),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable {
                                player.mediaController.pause()
                            }
                            .size(64.dp)
                    )
                }

                Image(
                    painter = painterResource(R.drawable.play_skip_forward),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable {
                            player.mediaController.seekToNext()
                        }
                        .padding(horizontal = 16.dp)
                        .size(32.dp)
                )


                Image(
                    painter = painterResource(
                        if (player.repeatMode == Player.REPEAT_MODE_ONE) {
                            R.drawable.repeat_one
                        } else {
                            R.drawable.repeat
                        }
                    ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (player.repeatMode == Player.REPEAT_MODE_OFF) {
                            colorPalette.textDisabled
                        } else {
                            colorPalette.text
                        }
                    ),
                    modifier = Modifier
                        .clickable {
                            player.mediaController.repeatMode =
                                (player.mediaController.repeatMode + 2) % 3

                            preferences.repeatMode = player.mediaController.repeatMode
                        }
                        .padding(horizontal = 16.dp)
                        .size(28.dp)
                )
            }
        }

        PlayerBottomSheet(
            layoutState = rememberBottomSheetState(64.dp, layoutState.upperBound - 128.dp),
            onGlobalRouteEmitted = layoutState.collapse,
            song = song,
            modifier = Modifier
                .padding(bottom = 128.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

