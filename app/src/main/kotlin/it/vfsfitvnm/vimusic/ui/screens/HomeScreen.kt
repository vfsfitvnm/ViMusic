package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.rememberRoute
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongCollection
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.SongWithInfo
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.views.PlayerView
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun HomeScreen(intentVideoId: String?) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val intentVideoRoute = rememberIntentVideoRoute(intentVideoId)
    val settingsRoute = rememberSettingsRoute()
    val playlistRoute = rememberLocalPlaylistRoute()
    val searchRoute = rememberSearchRoute()
    val searchResultRoute = rememberSearchResultRoute()
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val (route, onRouteChanged) = rememberRoute(intentVideoId?.let { intentVideoRoute })

    val playlistPreviews by remember {
        Database.playlistPreviews()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    val preferences = LocalPreferences.current

    val songCollection by remember(preferences.homePageSongCollection) {
        when (preferences.homePageSongCollection) {
            SongCollection.MostPlayed -> Database.mostPlayed()
            SongCollection.Favorites -> Database.favorites()
            SongCollection.History -> Database.history()
        }
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        RouteHandler(
            route = route,
            onRouteChanged = onRouteChanged,
            listenToGlobalEmitter = true
        ) {
            intentVideoRoute { videoId ->
                IntentVideoScreen(
                    videoId = videoId ?: error("videoId must be not null")
                )
            }

            settingsRoute {
                SettingsScreen()
            }

            playlistRoute { playlistId ->
                LocalPlaylistScreen(
                    playlistId = playlistId ?: error("playlistId cannot be null")
                )
            }

            searchResultRoute { query ->
                SearchResultScreen(
                    query = query,
                    onSearchAgain = {
                        searchRoute(query)
                    },
                )
            }

            searchRoute { initialTextInput ->
                SearchScreen(
                    initialTextInput = initialTextInput,
                    onSearch = { query ->
                        searchResultRoute(query)

                        coroutineScope.launch(Dispatchers.IO) {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                )
            }

            albumRoute { browseId ->
                AlbumScreen(
                    browseId = browseId ?: error("browseId cannot be null")
                )
            }

            artistRoute { browseId ->
                ArtistScreen(
                    browseId = browseId ?: error("browseId cannot be null")
                )
            }

            host {
                val player = LocalYoutubePlayer.current
                val menuState = LocalMenuState.current
                val density = LocalDensity.current

                val thumbnailSize = remember {
                    density.run {
                        54.dp.roundToPx()
                    }
                }

                var isCreatingANewPlaylist by rememberSaveable {
                    mutableStateOf(false)
                }

                if (isCreatingANewPlaylist) {
                    TextFieldDialog(
                        hintText = "Enter the playlist name",
                        onDismiss = {
                            isCreatingANewPlaylist = false
                        },
                        onDone = { text ->
                            coroutineScope.launch(Dispatchers.IO) {
                                Database.insert(Playlist(name = text))
                            }
                        }
                    )
                }

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier
                        .background(colorPalette.background)
                        .fillMaxSize()
                ) {
                    item {
                        TopAppBar(
                            modifier = Modifier
                                .height(52.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.cog),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        settingsRoute()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .size(24.dp)
                            )

                            Image(
                                painter = painterResource(R.drawable.search),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        searchRoute("")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .size(24.dp)
                            )
                        }
                    }

                    item {
                        BasicText(
                            text = "Your playlists",
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier
                                .height(248.dp)
                        ) {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(all = 8.dp)
                                        .width(108.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                isCreatingANewPlaylist = true
                                            }
                                            .background(colorPalette.lightBackground)
                                            .size(108.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.add),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(colorPalette.text),
                                            modifier = Modifier
                                                .size(24.dp)
                                        )
                                    }
                                }
                            }

                            items(playlistPreviews) { playlistPreview ->
                                PlaylistPreviewItem(
                                    playlistPreview = playlistPreview,
                                    modifier = Modifier
                                        .padding(all = 8.dp)
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            playlistRoute(playlistPreview.playlist.id)
                                        }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .zIndex(1f)
                                .padding(horizontal = 8.dp)
                                .padding(top = 32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                BasicText(
                                    text = when (preferences.homePageSongCollection) {
                                        SongCollection.MostPlayed -> "Most played"
                                        SongCollection.Favorites -> "Favorites"
                                        SongCollection.History -> "History"
                                    },
                                    style = typography.m.semiBold,
                                    modifier = Modifier
                                        .animateContentSize()
                                )

                                Image(
                                    painter = painterResource(R.drawable.repeat),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                    modifier = Modifier
                                        .clickable {
                                            val values = SongCollection.values()

                                            preferences.homePageSongCollection =
                                                values[(preferences.homePageSongCollection.ordinal + 1) % values.size]
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                        .size(16.dp)
                                )
                            }

                            Image(
                                painter = painterResource(R.drawable.ellipsis_horizontal),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        menuState.display {
                                            BasicMenu(onDismiss = menuState::hide) {
                                                MenuEntry(
                                                    icon = R.drawable.play,
                                                    text = "Play",
                                                    enabled = songCollection.isNotEmpty(),
                                                    onClick = {
                                                        menuState.hide()
                                                        YoutubePlayer.Radio.reset()
                                                        player?.mediaController?.forcePlayFromBeginning(
                                                            songCollection
                                                                .map(SongWithInfo::asMediaItem)
                                                        )
                                                    }
                                                )

                                                MenuEntry(
                                                    icon = R.drawable.shuffle,
                                                    text = "Shuffle",
                                                    enabled = songCollection.isNotEmpty(),
                                                    onClick = {
                                                        menuState.hide()
                                                        YoutubePlayer.Radio.reset()
                                                        player?.mediaController?.forcePlayFromBeginning(
                                                            songCollection
                                                                .shuffled()
                                                                .map(SongWithInfo::asMediaItem)
                                                        )
                                                    }
                                                )

                                                MenuEntry(
                                                    icon = R.drawable.time,
                                                    text = "Enqueue",
                                                    enabled = songCollection.isNotEmpty() && player?.playbackState == Player.STATE_READY,
                                                    onClick = {
                                                        menuState.hide()
                                                        player?.mediaController?.enqueue(
                                                            songCollection.map(SongWithInfo::asMediaItem)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                    itemsIndexed(
                        items = songCollection,
                        key = { _, song ->
                            song.song.id
                        }
                    ) { index, song ->
                        SongItem(
                            song = song,
                            thumbnailSize = thumbnailSize,
                            onClick = {
                                YoutubePlayer.Radio.reset()
                                player?.mediaController?.forcePlayAtIndex(
                                    songCollection.map(SongWithInfo::asMediaItem),
                                    index
                                )
                            },
                            menuContent = {
                                when (preferences.homePageSongCollection) {
                                    SongCollection.MostPlayed -> NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
                                    SongCollection.Favorites -> InFavoritesMediaItemMenu(song = song)
                                    SongCollection.History -> InHistoryMediaItemMenu(song = song)
                                }
                            },
                            onThumbnailContent = {
                                AnimatedVisibility(
                                    visible = preferences.homePageSongCollection == SongCollection.MostPlayed,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                ) {
                                    BasicText(
                                        text = song.song.formattedTotalPlayTime,
                                        style = typography.xxs.semiBold.center.color(Color.White),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.75f)
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        PlayerView(
            layoutState = rememberBottomSheetState(lowerBound = 64.dp, upperBound = maxHeight),
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}
