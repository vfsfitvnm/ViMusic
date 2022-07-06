package it.vfsfitvnm.vimusic.ui.views

import android.app.SearchManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.HorizontalTabPager
import it.vfsfitvnm.vimusic.ui.components.rememberTabPagerState
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.PlayerState
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@ExperimentalAnimationApi
@Composable
fun PlayerBottomSheet(
    playerState: PlayerState?,
    layoutState: BottomSheetState,
    song: Song?,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    val coroutineScope = rememberCoroutineScope()

    val tabPagerState = rememberTabPagerState(initialPageIndex = 0, pageCount = 2)

    var nextResult by remember(playerState?.mediaItem?.mediaId) {
        mutableStateOf<Result<YouTube.NextResult>?>(null)
    }

    BottomSheet(
        state = layoutState,
        peekHeight = 128.dp,
        elevation = 16.dp,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        handleOutsideInteractionsWhenExpanded = true,
        modifier = modifier,
        collapsedContent = {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layoutState.lowerBound)
                    .background(colorPalette.elevatedBackground)
            ) {
                Spacer(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = colorPalette.textDisabled,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .width(36.dp)
                        .height(4.dp)
                        .padding(top = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                ) {
                    @Composable
                    fun Element(
                        text: String,
                        pageIndex: Int
                    ) {
                        val color by animateColorAsState(
                            if (tabPagerState.pageIndex == pageIndex) {
                                colorPalette.text
                            } else {
                                colorPalette.textDisabled
                            }
                        )

                        val scale by animateFloatAsState(
                            if (pageIndex == pageIndex) {
                                1f
                            } else {
                                0.9f
                            }
                        )

                        BasicText(
                            text = text,
                            style = typography.xs.color(color).center,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.Main) {
                                            layoutState.expand()
                                            if (layoutState.isCollapsed) {
                                                tabPagerState.pageIndex = pageIndex
                                            } else {
                                                tabPagerState.animateScrollTo(pageIndex)
                                            }
                                        }
                                    }
                                )
                                .padding(vertical = 8.dp)
                                .scale(scale)
                                .weight(1f)
                        )
                    }

                    Element(
                        text = "UP NEXT",
                        pageIndex = 0
                    )

                    Element(
                        text = "LYRICS",
                        pageIndex = 1
                    )
                }
            }
        }
    ) {
        HorizontalTabPager(
            state = tabPagerState,
            modifier = Modifier
                .background(colorPalette.elevatedBackground)
                .fillMaxSize()
        ) { index ->
            when (index) {
                0 -> {
                    CurrentPlaylistView(
                        playerState = playerState,
                        layoutState = layoutState,
                        onGlobalRouteEmitted = onGlobalRouteEmitted,
                        modifier = Modifier
                            .padding(top = 64.dp)
                    )
                }
                1 -> {
                    val player = LocalPlayerServiceBinder.current?.player
                    val context = LocalContext.current

                    var lyricsResult by remember(song) {
                        mutableStateOf(song?.lyrics?.let { Result.success(it) })
                    }

                    LyricsView(
                        lyrics = lyricsResult?.getOrNull(),
                        nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                        onInitialize = {
                            coroutineScope.launch(Dispatchers.Main) {
                                val mediaItem = player?.currentMediaItem!!

                                if (nextResult == null) {
                                    val mediaItemIndex = player.currentMediaItemIndex

                                    nextResult = withContext(Dispatchers.IO) {
                                        YouTube.next(
                                            mediaItem.mediaId,
                                            mediaItem.mediaMetadata.extras?.getString("playlistId"),
                                            mediaItemIndex
                                        )
                                    }
                                }

                                lyricsResult = nextResult?.map { nextResult ->
                                    nextResult.lyrics?.text()?.getOrNull() ?: ""
                                }?.map { lyrics ->
                                    query {
                                        song?.let {
                                            Database.update(song.copy(lyrics = lyrics))
                                        } ?: Database.insert(mediaItem) { song ->
                                            song.copy(lyrics = lyrics)
                                        }
                                    }
                                    lyrics
                                }
                            }
                        },
                        onSearchOnline = {
                            val mediaMetadata = player?.mediaMetadata ?: return@LyricsView

                            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, "${mediaMetadata.title} ${mediaMetadata.artist} lyrics")
                            }

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "No browser app found!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLyricsUpdate = { lyrics ->
                            val mediaItem = player?.currentMediaItem
                            query {
                                song?.let {
                                    Database.update(song.copy(lyrics = lyrics))
                                } ?: mediaItem?.let {
                                    Database.insert(mediaItem) { song ->
                                        song.copy(lyrics = lyrics)
                                    }
                                }
                            }
                        }
                    )
                }
                else -> {}
            }
        }
    }
}
