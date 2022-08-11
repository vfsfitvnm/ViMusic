package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSection
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSectionSpacer
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownTextItem
import it.vfsfitvnm.vimusic.ui.components.themed.DropdownMenu
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.BuiltInPlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.add
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.drawCircle
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.isFirstLaunchKey
import it.vfsfitvnm.vimusic.utils.playlistGridExpandedKey
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import kotlinx.coroutines.Dispatchers

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen() {
    val (colorPalette, typography) = LocalAppearance.current

    val lazyListState = rememberLazyListState()
    val lazyHorizontalGridState = rememberLazyGridState()

    var playlistSortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var playlistSortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)
    var playlistGridExpanded by rememberPreference(playlistGridExpandedKey, false)

    val playlistPreviews by remember(playlistSortBy, playlistSortOrder) {
        Database.playlistPreviews(playlistSortBy, playlistSortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

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
            @Suppress("UNUSED_EXPRESSION") playlistPreviews
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
                contentPadding = WindowInsets.systemBars.asPaddingValues().add(bottom = Dimensions.collapsedPlayer),
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
                                .run {
                                    if (isFirstLaunch) {
                                        drawBehind {
                                            drawCircle(
                                                color = colorPalette.accent,
                                                center = Offset(
                                                    x = size.width,
                                                    y = 0.dp.toPx()
                                                ),
                                                radius = 4.dp.toPx(),
                                                shadow = Shadow(
                                                    color = colorPalette.accent,
                                                    blurRadius = 4.dp.toPx()
                                                )
                                            )
                                        }
                                    } else {
                                        this
                                    }
                                }
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
                            text = stringResource(R.string.your_playlists),
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
                                        text = stringResource(R.string.name),
                                        isSelected = playlistSortBy == PlaylistSortBy.Name,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortBy = PlaylistSortBy.Name
                                        }
                                    )

                                    DropDownTextItem(
                                        text = stringResource(R.string.date_added),
                                        isSelected = playlistSortBy == PlaylistSortBy.DateAdded,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            playlistSortBy = PlaylistSortBy.DateAdded
                                        }
                                    )

                                    DropDownTextItem(
                                        text = stringResource(R.string.song_count),
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
                                            SortOrder.Ascending -> stringResource(R.string.ascending)
                                            SortOrder.Descending -> stringResource(R.string.descending)
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
                                            true -> stringResource(R.string.collapse)
                                            false -> stringResource(R.string.compact)
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
                        item(key = "favorites") {
                            BuiltInPlaylistItem(
                                icon = R.drawable.heart,
                                colorTint = colorPalette.red,
                                name = stringResource(R.string.fav),
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { builtInPlaylistRoute(BuiltInPlaylist.Favorites) }
                                    )
                            )
                        }

                        item(key = "offline") {
                            BuiltInPlaylistItem(
                                icon = R.drawable.airplane,
                                colorTint = colorPalette.blue,
                                name = stringResource(R.string.offline),
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { builtInPlaylistRoute(BuiltInPlaylist.Offline) }
                                    )
                            )
                        }

                        items(
                            items = playlistPreviews,
                            key = { it.playlist.id },
                            contentType = { it }
                        ) { playlistPreview ->
                            PlaylistPreviewItem(
                                playlistPreview = playlistPreview,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(all = 8.dp)
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { localPlaylistRoute(playlistPreview.playlist.id) }
                                    )
                            )
                        }
                    }
                }

                item("songs") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(colorPalette.background0)
                            .zIndex(1f)
                            .padding(horizontal = 8.dp)
                            .padding(top = 32.dp)
                    ) {
                        BasicText(
                            text = stringResource(R.string.songs),
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

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

                        Box {
                            var isSortMenuDisplayed by remember {
                                mutableStateOf(false)
                            }

                            Image(
                                painter = painterResource(R.drawable.sort),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        isSortMenuDisplayed = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .size(20.dp)
                            )

                            DropdownMenu(
                                isDisplayed = isSortMenuDisplayed,
                                onDismissRequest = {
                                    isSortMenuDisplayed = false
                                }
                            ) {
                                DropDownSection {
                                    DropDownTextItem(
                                        text = stringResource(R.string.play_time),
                                        isSelected = songSortBy == SongSortBy.PlayTime,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            songSortBy = SongSortBy.PlayTime
                                        }
                                    )

                                    DropDownTextItem(
                                        text = stringResource(R.string.title),
                                        isSelected = songSortBy == SongSortBy.Title,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            songSortBy = SongSortBy.Title
                                        }
                                    )

                                    DropDownTextItem(
                                        text = stringResource(R.string.date_added),
                                        isSelected = songSortBy == SongSortBy.DateAdded,
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            songSortBy = SongSortBy.DateAdded
                                        }
                                    )
                                }

                                DropDownSectionSpacer()

                                DropDownSection {
                                    DropDownTextItem(
                                        text = when (songSortOrder) {
                                            SortOrder.Ascending -> stringResource(R.string.ascending)
                                            SortOrder.Descending -> stringResource(R.string.descending)
                                        },
                                        onClick = {
                                            isSortMenuDisplayed = false
                                            songSortOrder = !songSortOrder
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                itemsIndexed(
                    items = songCollection,
                    key = { _, song -> song.id },
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        swipeShow = true,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songCollection.map(DetailedSong::asMediaItem),
                                index
                            )
                        },
                        menuContent = {
                            InHistoryMediaItemMenu(song = song)
                        },
                        onThumbnailContent = {
                            AnimatedVisibility(
                                visible = songSortBy == SongSortBy.PlayTime,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                            ) {
                                BasicText(
                                    text = song.formattedTotalPlayTime,
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
                        },
                        modifier = Modifier
                            .animateItemPlacement()
                    )
                }
            }
        }
    }
}
