package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.fastFade
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongCollection
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.InFavoritesMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen() {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    val lazyListState = rememberLazyListState()

    val intentUriRoute = rememberIntentUriRoute()
    val settingsRoute = rememberSettingsRoute()
    val playlistRoute = rememberLocalPlaylistRoute()
    val searchRoute = rememberSearchRoute()
    val searchResultRoute = rememberSearchResultRoute()
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

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

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when (targetState.route) {
                settingsRoute -> scaleIn(initialScale = 0.9f) with fadeOut()
                else -> fastFade
            }
        }
    ) {
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
            // This somehow prevents items to not be displayed sometimes...
            @Suppress("UNUSED_EXPRESSION") playlistPreviews
            @Suppress("UNUSED_EXPRESSION") songCollection

            val binder = LocalPlayerServiceBinder.current
            val density = LocalDensity.current

            val thumbnailSize = remember {
                density.run {
                    54.dp.roundToPx()
                }
            }

            var isGridExpanded by remember {
                mutableStateOf(false)
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
                        query {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        BasicText(
                            text = "Your playlists",
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Image(
                            painter = painterResource(if (isGridExpanded) R.drawable.grid else R.drawable.grid_single),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                            modifier = Modifier
                                .clickable {
                                    isGridExpanded = !isGridExpanded
                                }
                                .padding(all = 10.dp)
                                .size(16.dp)
                        )
                    }
                }

                item {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(if (isGridExpanded) 3 else 1),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                            .height(124.dp * (if (isGridExpanded) 3 else 1))
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

                        items(
                            items = playlistPreviews,
                            contentType = { it }
                        ) { playlistPreview ->
                            PlaylistPreviewItem(
                                playlistPreview = playlistPreview,
                                modifier = Modifier
                                    .animateItemPlacement()
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
                            verticalAlignment = Alignment.Bottom,
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
                                    .alignByBaseline()
                                    .animateContentSize()
                            )

                            val songCollections = enumValues<SongCollection>()
                            val nextSongCollection =
                                songCollections[(preferences.homePageSongCollection.ordinal + 1) % songCollections.size]

                            BasicText(
                                text = when (nextSongCollection) {
                                    SongCollection.MostPlayed -> "Most played"
                                    SongCollection.Favorites -> "Favorites"
                                    SongCollection.History -> "History"
                                },
                                style = typography.xxs.secondary.bold,
                                modifier = Modifier
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            preferences.homePageSongCollection = nextSongCollection
                                        }
                                    )
                                    .alignByBaseline()
                                    .padding(horizontal = 16.dp)
                                    .animateContentSize()
                            )
                        }

                        Image(
                            painter = painterResource(R.drawable.shuffle),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable(enabled = songCollection.isNotEmpty()) {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayFromBeginning(
                                        songCollection
                                            .shuffled()
                                            .map(DetailedSong::asMediaItem)
                                    )
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
                    },
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songCollection.map(DetailedSong::asMediaItem),
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
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.75f)
                                                )
                                            ),
                                            shape = ThumbnailRoundness.shape
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
}
