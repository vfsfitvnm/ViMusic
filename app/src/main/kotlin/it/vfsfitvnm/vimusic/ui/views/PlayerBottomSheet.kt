package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.route.Route
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.empty
import it.vfsfitvnm.route.rememberRoute
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.Message
import it.vfsfitvnm.vimusic.ui.components.OutcomeItem
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.screens.rememberLyricsRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.LocalYoutubePlayer
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
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
    layoutState: BottomSheetState,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val player = LocalYoutubePlayer.current ?: return

    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    val coroutineScope = rememberCoroutineScope()

    val lyricsRoute = rememberLyricsRoute()

    var route by rememberRoute()

    var nextOutcome by remember(player.mediaItem!!.mediaId) {
        mutableStateOf<Outcome<YouTube.NextResult>>(Outcome.Initial)
    }

    var lyricsOutcome by remember(player.mediaItem!!.mediaId) {
        mutableStateOf<Outcome<String?>>(Outcome.Initial)
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
                        .background(color = colorPalette.textDisabled, shape = RoundedCornerShape(16.dp))
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
                OutcomeItem(
                    outcome = lyricsOutcome,
                    onInitialize = {
                        lyricsOutcome = Outcome.Loading

                        coroutineScope.launch(Dispatchers.Main) {
                            if (nextOutcome.isEvaluable) {
                                nextOutcome = Outcome.Loading
                                nextOutcome = withContext(Dispatchers.IO) {
                                    YouTube.next(
                                        player.mediaItem!!.mediaId,
                                        player.mediaItem!!.mediaMetadata.extras?.getString("playlistId"),
                                        player.mediaItemIndex
                                    )
                                }
                            }

                            lyricsOutcome = nextOutcome.flatMap {
                                it.lyrics?.text().toNotNull()
                            }
                        }
                    },
                    onLoading = {
                        LyricsShimmer(
                            modifier = Modifier
                                .shimmer()
                        )
                    }
                ) { lyrics ->
                    if (lyrics != null) {
                        BasicText(
                            text = lyrics,
                            style = typography.xs.center,
                            modifier = Modifier
                                .padding(top = 64.dp)
                                .nestedScroll(remember { layoutState.nestedScrollConnection() })
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .padding(horizontal = 48.dp)
                        )
                    } else {
                        Message(
                            text = "Lyrics not available",
                            icon = R.drawable.text,
                            modifier = Modifier
                                .padding(top = 64.dp)
                        )
                    }
                }
            }

            host {
                CurrentPlaylistView(
                    layoutState = layoutState,
                    onGlobalRouteEmitted = onGlobalRouteEmitted,
                    modifier = Modifier
                        .padding(top = 64.dp)
                )
            }
        }
    }
}

@Composable
fun LyricsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        repeat(16) { index ->
            TextPlaceholder(
                modifier = Modifier
                    .alpha(1f - index * 0.05f)
            )
        }
    }
}