package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.verticalDragAfterLongPressToReorder
import it.vfsfitvnm.reordering.verticalDragToReorder
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.MusicBars
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.screens.SmallSongItemShimmer
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.add
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberMediaItemIndex
import it.vfsfitvnm.vimusic.utils.rememberShouldBePlaying
import it.vfsfitvnm.vimusic.utils.rememberWindows
import it.vfsfitvnm.vimusic.utils.shuffleQueue

@ExperimentalAnimationApi
@Composable
fun CurrentPlaylistView(
    layoutState: BottomSheetState,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val binder = LocalPlayerServiceBinder.current
    val hapticFeedback = LocalHapticFeedback.current
    val (colorPalette, typography) = LocalAppearance.current

    binder?.player ?: return

    val thumbnailSize = Dimensions.thumbnails.song.px

    val mediaItemIndex by rememberMediaItemIndex(binder.player)
    val windows by rememberWindows(binder.player)
    val shouldBePlaying by rememberShouldBePlaying(binder.player)

    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex)

    val reorderingState = rememberReorderingState(windows)

    val paddingValues = WindowInsets.systemBars.asPaddingValues()
    val bottomPadding = paddingValues.calculateBottomPadding()

    Column {
        LazyColumn(
            state = lazyListState,
            contentPadding = paddingValues.add(bottom = -bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .nestedScroll(remember {
                    layoutState.nestedScrollConnection(lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
                })
                .background(colorPalette.background1)
                .weight(1f)
        ) {
            items(
                items = windows,
                key = { it.uid.hashCode() }
            ) { window ->
                val isPlayingThisMediaItem = mediaItemIndex == window.firstPeriodIndex

                SongItem(
                    swipeShow = false,
                    mediaItem = window.mediaItem,
                    thumbnailSize = thumbnailSize,
                    onClick = {
                        if (isPlayingThisMediaItem) {
                            if (shouldBePlaying) {
                                binder.player.pause()
                            } else {
                                binder.player.play()
                            }
                        } else {
                            binder.player.playWhenReady = true
                            binder.player.seekToDefaultPosition(window.firstPeriodIndex)
                        }
                    },
                    menuContent = {
                        QueuedMediaItemMenu(
                            mediaItem = window.mediaItem,
                            indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
                            onGlobalRouteEmitted = onGlobalRouteEmitted
                        )
                    },
                    onThumbnailContent = {
                        androidx.compose.animation.AnimatedVisibility(
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
                                    .size(Dimensions.thumbnails.song)
                            ) {
                                if (shouldBePlaying) {
                                    MusicBars(
                                        color = colorPalette.onOverlay,
                                        modifier = Modifier
                                            .height(24.dp)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.play),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorPalette.onOverlay),
                                        modifier = Modifier
                                            .size(24.dp)
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
                    modifier = Modifier
//                        .animateItemPlacement()
                        .verticalDragAfterLongPressToReorder(
                            reorderingState = reorderingState,
                            lazyListState = lazyListState,
                            index = window.firstPeriodIndex,
                            onDragStart = {
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },
                            onDragEnd = { reachedIndex ->
                                binder.player.moveMediaItem(window.firstPeriodIndex, reachedIndex)
                            }
                        )
                )
            }

            item {
                if (binder.isLoadingRadio) {
                    Column(
                        modifier = Modifier
                            .shimmer()
                    ) {
                        repeat(3) { index ->
                            SmallSongItemShimmer(
                                thumbnailSizeDp = Dimensions.thumbnails.song,
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

        Box(
            modifier = Modifier
                .clickable(
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = layoutState::collapseSoft
                )
                .height(64.dp + bottomPadding)
                .background(colorPalette.background2)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = bottomPadding)
        ) {
            BasicText(
                text = "${windows.size} songs",
                style = typography.xxs.medium,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .background(color = colorPalette.background1, shape = RoundedCornerShape(16.dp))
                    .align(Alignment.CenterStart)
                    .padding(all = 8.dp)
            )
            Image(
                painter = painterResource(R.drawable.chevron_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(18.dp)
            )
            Image(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .padding(end = 2.dp)
                    .clickable(onClick = binder.player::shuffleQueue)
                    .align(Alignment.CenterEnd)
                    .padding(all = 8.dp)
                    .size(20.dp)
            )
        }
    }
}
