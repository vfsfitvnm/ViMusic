package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.reordering.ReorderingLazyColumn
import it.vfsfitvnm.reordering.animateItemPlacement
import it.vfsfitvnm.reordering.draggedItem
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.reorder
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.MusicBars
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.QueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberMediaItemIndex
import it.vfsfitvnm.vimusic.utils.rememberShouldBePlaying
import it.vfsfitvnm.vimusic.utils.rememberWindows
import it.vfsfitvnm.vimusic.utils.shuffleQueue
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PlayerBottomSheet(
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(backgroundColorProvider()) }
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                Image(
                    painter = painterResource(R.drawable.playlist),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp)
                )

                content()
            }
        }
    ) {
        val binder = LocalPlayerServiceBinder.current
        val layoutDirection = LocalLayoutDirection.current

        binder?.player ?: return@BottomSheet

        val menuState = LocalMenuState.current

        val thumbnailSizeDp = Dimensions.thumbnails.song
        val thumbnailSizePx = thumbnailSizeDp.px

        val mediaItemIndex by rememberMediaItemIndex(binder.player)
        val windows by rememberWindows(binder.player)
        val shouldBePlaying by rememberShouldBePlaying(binder.player)

        val reorderingState = rememberReorderingState(
            lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex),
            key = windows,
            onDragEnd = { fromIndex, toIndex ->
                binder.player.moveMediaItem(fromIndex, toIndex)
            },
            extraItemCount = 0
        )

        val paddingValues = WindowInsets.systemBars.asPaddingValues()
        val bottomPadding = paddingValues.calculateBottomPadding()

        val rippleIndication = rememberRipple(bounded = false)

        Column {
            ReorderingLazyColumn(
                reorderingState = reorderingState,
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
                    .nestedScroll(remember {
                        layoutState.nestedScrollConnection(reorderingState.lazyListState.firstVisibleItemIndex == 0 && reorderingState.lazyListState.firstVisibleItemScrollOffset == 0)
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
                        song = window.mediaItem,
                        thumbnailSizePx = thumbnailSizePx,
                        thumbnailSizeDp = thumbnailSizeDp,
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
                                            shape = thumbnailShape
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
                            IconButton(
                                icon = R.drawable.reorder,
                                color = colorPalette.textDisabled,
                                indication = rippleIndication,
                                onClick = {},
                                modifier = Modifier
                                    .reorder(reorderingState = reorderingState, index = window.firstPeriodIndex)
                                    .size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    menuState.display {
                                        QueuedMediaItemMenu(
                                            mediaItem = window.mediaItem,
                                            indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                },
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
                                }
                            )
                            .animateItemPlacement(reorderingState = reorderingState)
                            .draggedItem(
                                reorderingState = reorderingState,
                                index = window.firstPeriodIndex
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
                                SongItemPlaceholder(
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
                    .clickable(onClick = layoutState::collapseSoft)
                    .height(64.dp + bottomPadding)
                    .background(colorPalette.background2)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = bottomPadding)
            ) {
                BasicText(
                    text = "${windows.size} songs",
                    style = typography.xxs.medium,
                    modifier = Modifier
                        .background(
                            color = colorPalette.background1,
                            shape = RoundedCornerShape(16.dp)
                        )
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

                IconButton(
                    icon = R.drawable.shuffle,
                    color = colorPalette.text,
                    onClick = {
                        reorderingState.coroutineScope.launch {
                            reorderingState.lazyListState.smoothScrollToTop()
                        }.invokeOnCompletion {
                            binder.player.shuffleQueue()
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .size(20.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}
