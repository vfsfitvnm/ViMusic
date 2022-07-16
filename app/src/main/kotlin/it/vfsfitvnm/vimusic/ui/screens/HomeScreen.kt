package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.*
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.*
import it.vfsfitvnm.vimusic.ui.styling.*
import it.vfsfitvnm.vimusic.ui.views.*
import it.vfsfitvnm.vimusic.utils.*


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen() {
    val intentUriRoute = rememberIntentUriRoute()
    val settingsRoute = rememberSettingsRoute()
    val playlistRoute = rememberLocalPlaylistRoute()
    val builtInPlaylistRoute = rememberBuiltInPlaylistRoute()
    val searchRoute = rememberSearchRoute()
    val searchResultRoute = rememberSearchResultRoute()
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    RouteHandler(listenToGlobalEmitter = true) {
        settingsRoute {
            SettingsScreen()
        }

        playlistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    searchResultRoute(query)

                    query {
                        Database.insert(SearchQuery(query = query))
                    }
                },
                onUri = { uri ->
                    intentUriRoute(uri)
                }
            )
        }

        albumRoute { browseId ->
            AlbumScreen(browseId = browseId ?: error("browseId cannot be null"))
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        intentUriRoute { uri ->
            IntentUriScreen(
                uri = uri ?: Uri.EMPTY
            )
        }

        host {
            val (colorPalette, typography) = LocalAppearance.current

            val isFirstLaunch by rememberPreference(isFirstLaunchKey, true)

            val tabPagerState = rememberTabPagerState(
                pageIndexState = rememberPreference(homeScreenPageIndexKey, 0),
                pageCount = 4
            )

            val density = LocalDensity.current

            var topAppBarOffset by remember {
                mutableStateOf(0.dp)
            }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        val newOffset = topAppBarOffset + with(density) { available.y.toDp() }
                        val coerced =
                            newOffset.coerceIn(minimumValue = (-52).dp, maximumValue = 0.dp)
                        return if (newOffset == coerced) {
                            topAppBarOffset = coerced
                            available.copy(x = 0f)
                        } else {
                            Offset.Zero
                        }
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        topAppBarOffset = (topAppBarOffset + with(density) { consumed.y.toDp() })
                            .coerceIn(minimumValue = (-52).dp, maximumValue = 0.dp)
                        return Offset.Zero
                    }
                }
            }

            Box {
                TopAppBar(
                    modifier = Modifier
                        .offset(y = topAppBarOffset)
                        .height(52.dp)
                ) {
                    BasicText(
                        text = "ViMusic",
                        style = typography.l.semiBold,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    settingsRoute()
                                }
                            )
                            .drawBehind {
                                drawCircle(
                                    color = colorPalette.primaryContainer,
                                    center = size.center.copy(x = 8.dp.toPx()),
                                    radius = 16.dp.toPx()
                                )

                                drawCircle(
                                    color = colorPalette.primaryContainer,
                                    center = Offset(x = 32.dp.toPx(), y = 0f),
                                    radius = 8.dp.toPx()
                                )

                                if (!isFirstLaunch) return@drawBehind

                                drawCircle(
                                    color = colorPalette.red,
                                    center = Offset(
                                        x = size.width - 8.dp.toPx(),
                                        y = 0.dp.toPx()
                                    ),
                                    radius = 4.dp.toPx(),
                                    shadow = Shadow(
                                        color = colorPalette.red,
                                        blurRadius = 4.dp.toPx()
                                    )
                                )
                            }
                            .padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    searchRoute("")
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .size(24.dp)
                        )

//                        Image(
//                            painter = painterResource(R.drawable.cog),
//                            contentDescription = null,
//                            colorFilter = ColorFilter.tint(colorPalette.text),
//                            modifier = Modifier
//                                .clickable {
//                                    settingsRoute()
//                                }
//                                .padding(horizontal = 8.dp, vertical = 8.dp)
//                                .run {
//                                    if (isFirstLaunch) {
//                                        drawBehind {
//                                            drawCircle(
//                                                color = colorPalette.red,
//                                                center = Offset(
//                                                    x = size.width,
//                                                    y = 0.dp.toPx()
//                                                ),
//                                                radius = 4.dp.toPx(),
//                                                shadow = Shadow(
//                                                    color = colorPalette.red,
//                                                    blurRadius = 4.dp.toPx()
//                                                )
//                                            )
//                                        }
//                                    } else {
//                                        this
//                                    }
//                                }
//                                .size(24.dp)
//                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .nestedScroll(nestedScrollConnection)
                        .padding(top = 52.dp + topAppBarOffset)
                ) {
                    TabRow(tabPagerState = tabPagerState) {
                        TabRowItem(
                            tabPagerState = tabPagerState,
                            index = 0,
                            text = "Songs"
                        )

                        TabRowItem(
                            tabPagerState = tabPagerState,
                            index = 1,
                            text = "Playlists"
                        )

                        TabRowItem(
                            tabPagerState = tabPagerState,
                            index = 2,
                            text = "Artists"
                        )

                        TabRowItem(
                            tabPagerState = tabPagerState,
                            index = 3,
                            text = "Albums"
                        )
                    }

                    HorizontalTabPager(
                        state = tabPagerState,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxSize()
                    ) { index ->
                        when (index) {
                            1 -> PlaylistsTab(
                                onBuiltInPlaylistClicked = { builtInPlaylist ->
                                    builtInPlaylistRoute(builtInPlaylist)
                                },
                                onPlaylistClicked = { playlist ->
                                    playlistRoute(playlist.id)
                                }
                            )
                            2 -> ArtistsTab(
                                onArtistClicked = { artist ->
                                    artistRoute(artist.id)
                                }
                            )
                            3 -> AlbumsTab(
                                onAlbumClicked = { album ->
                                    albumRoute(album.id)
                                }
                            )
                            else -> SongsTab()
                        }
                    }
                }
            }
        }
    }
}
