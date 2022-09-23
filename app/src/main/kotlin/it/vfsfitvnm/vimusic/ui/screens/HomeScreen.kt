package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.FavoritePlaylistItem
import it.vfsfitvnm.vimusic.models.OfflinePlaylistItem
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.RealPlaylistItem
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.badge
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSection
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSectionSpacer
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownTextItem
import it.vfsfitvnm.vimusic.ui.components.themed.DropdownMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.BuiltInPlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.utils.isFirstLaunchKey
import it.vfsfitvnm.vimusic.utils.playlistGridExpandedKey
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen() {
    val (colorPalette, typography) = LocalAppearance.current

    val lazyListState = rememberLazyListState()
    val lazyHorizontalGridState = rememberLazyGridState()

    var playlistSortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var playlistSortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Ascending)
    var playlistGridExpanded by rememberPreference(playlistGridExpandedKey, false)

    val playlistItems by remember(playlistSortBy, playlistSortOrder) {
        combine(
            Database.playlistPreviews(playlistSortBy, playlistSortOrder)
                .map {
                    it.map { RealPlaylistItem(it) }
                },
            flowOf(
                listOf(
                    FavoritePlaylistItem, OfflinePlaylistItem
                )
            )
        ) { realItems, artificialItems ->
            realItems + artificialItems
        }
    }.collectAsState(initial = emptyList(), context = Dispatchers.Main)

    var songSortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var songSortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    val songCollection by remember(songSortBy, songSortOrder) {
        Database.songs(songSortBy, songSortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    RouteHandler(listenToGlobalEmitter = true) {
        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
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
            // This somehow prevents items to not be displayed sometimes...
            @Suppress("UNUSED_EXPRESSION") playlistItems
            @Suppress("UNUSED_EXPRESSION") songCollection

            val binder = LocalPlayerServiceBinder.current

            val isFirstLaunch by rememberPreference(isFirstLaunchKey, true)

            val thumbnailSize = Dimensions.thumbnails.song.px

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
                contentPadding = LocalPlayerAwarePaddingValues.current,
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item("topAppBar") {
                    TopAppBar(
                        modifier = Modifier
                            .height(52.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.equalizer),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable { settingsRoute() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .badge(color = colorPalette.red, isDisplayed = isFirstLaunch)
                                .size(24.dp)
                        )

                        Image(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable { searchRoute("") }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .size(24.dp)
                        )
                    }
                }

                item("playlistsHeader") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(horizontal = 8.dp)
                            .padding(top = 16.dp)
                    ) {
                        BasicText(
                            text = "Playlists",
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Image(
                            painter = painterResource(R.drawable.add),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable { isCreatingANewPlaylist = true }
                                .padding(all = 8.dp)
                                .size(20.dp)
                        )

                        Box {
                            var isSortMenuDisplayed by remember {
                                mutableStateOf(false)
                            }

                            Image(
                                painter = painterResource(R.drawable.sort),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable { isSortMenuDisplayed = true }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .size(20.dp)
                            )

                            DropdownMenu(
                                isDisplayed = isSortMenuDisplayed,
                                onDismissRequest = { isSortMenuDisplayed = false }
                            ) {
                                DropDownSection {
                                    DropDownTextItem(
                                        text = "NAME",
                                        isSelected = playlistSortBy == PlaylistSortBy.Name,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortBy = PlaylistSortBy.Name
                                        }
                                    )

                                    DropDownTextItem(
                                        text = "DATE ADDED",
                                        isSelected = playlistSortBy == PlaylistSortBy.DateAdded,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortBy = PlaylistSortBy.DateAdded
                                        }
                                    )

                                    DropDownTextItem(
                                        text = "SONG COUNT",
                                        isSelected = playlistSortBy == PlaylistSortBy.SongCount,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortBy = PlaylistSortBy.SongCount
                                        }
                                    )
                                }

                                DropDownSectionSpacer()

                                DropDownSection {
                                    DropDownTextItem(
                                        text = when (playlistSortOrder) {
                                            SortOrder.Ascending -> "ASCENDING"
                                            SortOrder.Descending -> "DESCENDING"
                                        },
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortOrder = !playlistSortOrder
                                        }
                                    )
                                }
                                DropDownSectionSpacer()

                                DropDownSection {
                                    DropDownTextItem(
                                        text = when (playlistGridExpanded) {
                                            true -> "COLLAPSE"
                                            false -> "EXPAND"
                                        },
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistGridExpanded = !playlistGridExpanded
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item("playlists") {
                    LazyHorizontalGrid(
                        state = lazyHorizontalGridState,
                        rows = GridCells.Fixed(if (playlistGridExpanded) 3 else 1),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                            .height(124.dp * (if (playlistGridExpanded) 3 else 1))
                    ) {
                        items(
                            items = playlistItems,
                            key = { it.contentId },
                            contentType = { it }
                        ) { item ->
                            when (item) {
                                is RealPlaylistItem -> PlaylistPreviewItem(
                                    playlistPreview = item.playlistPreview,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(all = 8.dp)
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { localPlaylistRoute(item.playlistPreview.playlist.id) }
                                        )
                                )
                                FavoritePlaylistItem -> BuiltInPlaylistItem(
                                    icon = R.drawable.heart,
                                    colorTint = colorPalette.red,
                                    name = item.title,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(all = 8.dp)
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { builtInPlaylistRoute(BuiltInPlaylist.Favorites) }
                                        )
                                )
                                OfflinePlaylistItem -> BuiltInPlaylistItem(
                                    icon = R.drawable.airplane,
                                    colorTint = colorPalette.blue,
                                    name = item.title,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(all = 8.dp)
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { builtInPlaylistRoute(BuiltInPlaylist.Offline) }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
