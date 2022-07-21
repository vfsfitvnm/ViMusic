package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LightColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.PlayerState
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold


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
    val (colorPalette, typography) = LocalAppearance.current

    val thumbnailSize = Dimensions.thumbnails.song.px

    val isPaused by derivedStateOf {
        playerState?.playbackState == Player.STATE_ENDED || playerState?.playWhenReady == false
    }

    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = playerState?.mediaItemIndex ?: 0)

    val reorderingState = rememberReorderingState(playerState?.mediaItems ?: emptyList())

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .nestedScroll(remember {
                    layoutState.nestedScrollConnection(lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
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
                                    .size(Dimensions.thumbnails.song)
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
                    backgroundColor = colorPalette.background,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .clickable(
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = layoutState.collapse
                )
                .shadow(elevation = 8.dp)
                .height(64.dp)
                .background(colorPalette.elevatedBackground)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(R.drawable.chevron_up),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .rotate(180f)
                    .padding(all = 16.dp)
                    .size(18.dp)
            )

            Column {
                BasicText(
                    text = "Queue",
                    style = typography.s.medium,
                    modifier = Modifier
                )
                BasicText(
                    text = "${playerState?.mediaItems?.size ?: 0} songs",
                    style = typography.xxs.semiBold.secondary,
                    modifier = Modifier
                )
            }

            Spacer(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .size(18.dp)
            )
        }
    }
}
