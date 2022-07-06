package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.verticalDragAfterLongPressToReorder
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.MusicBars
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.screens.SmallSongItemShimmer
import it.vfsfitvnm.vimusic.ui.styling.LightColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.utils.PlayerState


@ExperimentalAnimationApi
@Composable
fun CurrentPlaylistView(
    playerState: PlayerState?,
    layoutState: BottomSheetState,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val binder = LocalPlayerServiceBinder.current
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val colorPalette = LocalColorPalette.current

    val thumbnailSize = remember {
        density.run {
            54.dp.roundToPx()
        }
    }

    val isPaused by derivedStateOf {
        playerState?.playbackState == Player.STATE_ENDED || playerState?.playWhenReady == false
    }

    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = playerState?.mediaItemIndex ?: 0)

    val reorderingState = rememberReorderingState(playerState?.mediaItems ?: emptyList())

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .nestedScroll(remember {
                layoutState.nestedScrollConnection(playerState?.mediaItemIndex == 0)
            })
    ) {
        itemsIndexed(
            items = playerState?.mediaItems ?: emptyList()
        ) { index, mediaItem ->
            val isPlayingThisMediaItem by derivedStateOf {
                playerState?.mediaItemIndex == index
            }

            SongItem(
                mediaItem = mediaItem,
                thumbnailSize = thumbnailSize,
                onClick = {
                    if (isPlayingThisMediaItem) {
                        if (isPaused) {
                            binder?.player?.play()
                        } else {
                            binder?.player?.pause()
                        }
                    } else {
                        binder?.player?.playWhenReady = true
                        binder?.player?.seekToDefaultPosition(index)
                    }
                },
                menuContent = {
                    QueuedMediaItemMenu(
                        mediaItem = mediaItem,
                        indexInQueue = if (isPlayingThisMediaItem) null else index,
                        onGlobalRouteEmitted = onGlobalRouteEmitted
                    )
                },
                onThumbnailContent = {
                    AnimatedVisibility(
                        visible = isPlayingThisMediaItem,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    shape = ThumbnailRoundness.shape
                                )
                                .size(54.dp)
                        ) {
                            if (isPaused) {
                                Image(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(LightColorPalette.background),
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            } else {
                                MusicBars(
                                    color = LightColorPalette.background,
                                    modifier = Modifier
                                        .height(24.dp)
                                )
                            }
                        }
                    }
                },
                trailingContent = {
                    Image(
                        painter = painterResource(R.drawable.reorder),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                        modifier = Modifier
                            .clickable {}
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .size(20.dp)
                    )
                },
                backgroundColor = colorPalette.elevatedBackground,
                modifier = Modifier
                    .verticalDragAfterLongPressToReorder(
                        reorderingState = reorderingState,
                        index = index,
                        onDragStart = {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        },
                        onDragEnd = { reachedIndex ->
                            binder?.player?.moveMediaItem(index, reachedIndex)
                        }
                    )
            )
        }

        item {
            if (binder?.isLoadingRadio == true) {
                Column(
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(3) { index ->
                        SmallSongItemShimmer(
                            thumbnailSizeDp = 54.dp,
                            modifier = Modifier
                                .alpha(1f - index * 0.125f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
