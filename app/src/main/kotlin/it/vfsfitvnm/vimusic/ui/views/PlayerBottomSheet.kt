package it.vfsfitvnm.vimusic.ui.views

import android.app.SearchManager
import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.with
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
import androidx.media3.common.Player
import it.vfsfitvnm.route.Route
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.empty
import it.vfsfitvnm.route.rememberRoute
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.screens.rememberLyricsRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.isEvaluable
import it.vfsfitvnm.youtubemusic.toNotNull
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

    val lyricsRoute = rememberLyricsRoute()

    var route by rememberRoute()

    var nextOutcome by remember(playerState?.mediaItem?.mediaId) {
        mutableStateOf<Outcome<YouTube.NextResult>>(Outcome.Initial)
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
                        targetRoute: Route?
                    ) {
                        val color by animateColorAsState(
                            if (targetRoute == route) {
                                colorPalette.text
                            } else {
                                colorPalette.textDisabled
                            }
                        )

                        val scale by animateFloatAsState(
                            if (targetRoute == route) {
                                1f
                            } else {
                                0.9f
                            }
                        )

                        BasicText(
                            text = text,
                            style = typography.xs.medium.color(color).center,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    route = targetRoute
                                    coroutineScope.launch(Dispatchers.Main) {
                                        layoutState.expand()
                                    }
                                }
                                .padding(vertical = 8.dp)
                                .scale(scale)
                                .weight(1f)
                        )
                    }

                    Element(
                        text = "UP NEXT",
                        targetRoute = null
                    )

                    Element(
                        text = "LYRICS",
                        targetRoute = lyricsRoute
                    )
                }
            }
        }
    ) {
        var lyricsOutcome by remember(song) {
            mutableStateOf(song?.lyrics?.let { Outcome.Success(it) } ?: Outcome.Initial)
        }

        RouteHandler(
            route = route,
            onRouteChanged = {
                route = it
            },
            handleBackPress = false,
            transitionSpec = {
                when (targetState.route) {
                    lyricsRoute -> slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)
                    else -> when (initialState.route) {
                        lyricsRoute -> slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
                        else -> empty
                    }
                }
            },
            modifier = Modifier
                .background(colorPalette.elevatedBackground)
                .fillMaxSize()
        ) {
            lyricsRoute {
                val player = LocalPlayerServiceBinder.current?.player

                val context = LocalContext.current

                LyricsView(
                    lyricsOutcome = lyricsOutcome,
                    nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                    onInitialize = {
                        coroutineScope.launch(Dispatchers.Main) {
                            lyricsOutcome = Outcome.Loading

                            val mediaItem = player?.currentMediaItem!!

                            if (nextOutcome.isEvaluable) {
                                nextOutcome = Outcome.Loading


                                val mediaItemIndex = player.currentMediaItemIndex

                                nextOutcome = withContext(Dispatchers.IO) {
                                    YouTube.next(
                                        mediaItem.mediaId,
                                        mediaItem.mediaMetadata.extras?.getString("playlistId"),
                                        mediaItemIndex
                                    )
                                }
                            }

                            lyricsOutcome = nextOutcome.flatMap {
                                it.lyrics?.text().toNotNull()
                            }.map { lyrics ->
                                lyrics ?: ""
                            }.map { lyrics ->
                                withContext(Dispatchers.IO) {
                                    (song ?: mediaItem.let(Database::insert)).let {
                                        Database.update(it.copy(lyrics = lyrics))
                                    }
                                }
                                lyrics
                            }
                        }
                    },
                    onSearchOnline = {
                        player?.mediaMetadata?.let {
                            context.startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(
                                    SearchManager.QUERY,
                                    "${it.title} ${it.artist} lyrics"
                                )
                            })
                        }
                    },
                    onLyricsUpdate = { lyrics ->
                        val mediaItem = player?.currentMediaItem
                        coroutineScope.launch(Dispatchers.IO) {
                            (song ?: mediaItem?.let(Database::insert))?.let {
                                Database.update(it.copy(lyrics = lyrics))
                            }
                        }
                    }
                )
            }

            host {
                CurrentPlaylistView(
                    playerState = playerState,
                    layoutState = layoutState,
                    onGlobalRouteEmitted = onGlobalRouteEmitted,
                    modifier = Modifier
                        .padding(top = 64.dp)
                )
            }
        }
    }
}
