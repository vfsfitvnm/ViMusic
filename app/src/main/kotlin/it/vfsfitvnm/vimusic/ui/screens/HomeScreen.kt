package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.*
import it.vfsfitvnm.vimusic.models.*
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.*
import it.vfsfitvnm.vimusic.ui.components.themed.DropdownMenu
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.*
import it.vfsfitvnm.vimusic.ui.views.BuiltInPlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


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

            var homeScreenPageIndex by rememberPreference(homeScreenPageIndexKey, 0)
            val isFirstLaunch by rememberPreference(isFirstLaunchKey, true)

            val tabPagerState = rememberTabPagerState(
                initialPageIndex = homeScreenPageIndex,
                pageCount = 4
            ) {
                homeScreenPageIndex = it
            }

            val coroutineScope = rememberCoroutineScope()
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
                            .drawBehind {
                                drawCircle(
                                    color = colorPalette.primaryContainer,
                                    center = size.center.copy(x = 8.dp.toPx()),
                                    radius = 16.dp.toPx()
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

                        Image(
                            painter = painterResource(R.drawable.cog),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    settingsRoute()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .run {
                                    if (isFirstLaunch) {
                                        drawBehind {
                                            drawCircle(
                                                color = colorPalette.red,
                                                center = Offset(
                                                    x = size.width,
                                                    y = 0.dp.toPx()
                                                ),
                                                radius = 4.dp.toPx(),
                                                shadow = Shadow(
                                                    color = colorPalette.red,
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
                    }
                }

                Column(
                    modifier = Modifier
                        .nestedScroll(nestedScrollConnection)
                        .padding(top = 52.dp + topAppBarOffset)
                ) {
                    TabRow(tabPagerState = tabPagerState) {
                        @Composable
                        fun Item(
                            index: Int,
                            text: String
                        ) {
                            val alpha by animateFloatAsState(
                                if (tabPagerState.transitioningIndex == index) {
                                    1f
                                } else {
                                    0.4f
                                }
                            )

                            val scale by animateFloatAsState(
                                if (tabPagerState.transitioningIndex == index) {
                                    1f
                                } else {
                                    0.9f
                                }
                            )

                            BasicText(
                                text = text,
                                style = typography.s.semiBold.color(colorPalette.text),
                                modifier = Modifier
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            coroutineScope.launch {
                                                tabPagerState.animateScrollTo(index)
                                            }
                                        }
                                    )
                                    .graphicsLayer {
                                        this.alpha = alpha
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Item(
                            index = 0,
                            text = "Songs"
                        )

                        Item(
                            index = 1,
                            text = "Playlists"
                        )

                        Item(
                            index = 2,
                            text = "Artists"
                        )

                        Item(
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SongsTab() {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("songSortBy", SongSortBy.DateAdded)
    var sortOrder by rememberPreference("songSortOrder", SortOrder.Ascending)

    val songs by remember(sortBy, sortOrder) {
        Database.songs(sortBy, sortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    Box {
        val binder = LocalPlayerServiceBinder.current
        val thumbnailSize = Dimensions.thumbnails.song.px

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorPalette.background,
                            Color.Transparent
                        )
                    )
                )
                .fillMaxWidth()
                .zIndex(1f)
                .padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(enabled = songs.isNotEmpty()) {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs
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
                    @Composable
                    fun Item(
                        text: String,
                        textColor: Color,
                        backgroundColor: Color,
                        onClick: () -> Unit
                    ) {
                        BasicText(
                            text = text,
                            style = typography.xxs.medium.copy(
                                color = textColor,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = rememberRipple(
                                        bounded = true
                                    ),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        isSortMenuDisplayed = false
                                        onClick()
                                    }
                                )
                                .background(backgroundColor)
                                .fillMaxWidth()
                                .widthIn(min = 124.dp, max = 248.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                        )
                    }

                    @Composable
                    fun Item(
                        text: String,
                        isSelected: Boolean,
                        onClick: () -> Unit
                    ) {
                        Item(
                            text = text,
                            textColor = if (isSelected) {
                                colorPalette.onPrimaryContainer
                            } else {
                                colorPalette.textSecondary
                            },
                            backgroundColor = if (isSelected) {
                                colorPalette.primaryContainer
                            } else {
                                colorPalette.elevatedBackground
                            },
                            onClick = onClick
                        )
                    }

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = "PLAY TIME",
                            isSelected = sortBy == SongSortBy.PlayTime,
                            onClick = {
                                sortBy = SongSortBy.PlayTime
                            }
                        )
                        Item(
                            text = "TITLE",
                            isSelected = sortBy == SongSortBy.Title,
                            onClick = {
                                sortBy = SongSortBy.Title
                            }
                        )
                        Item(
                            text = "DATE ADDED",
                            isSelected = sortBy == SongSortBy.DateAdded,
                            onClick = {
                                sortBy = SongSortBy.DateAdded
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = when (sortOrder) {
                                SortOrder.Ascending -> "ASCENDING"
                                SortOrder.Descending -> "DESCENDING"
                            },
                            textColor = colorPalette.text,
                            backgroundColor = colorPalette.elevatedBackground,
                            onClick = {
                                sortOrder = !sortOrder
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(top = 36.dp)) {
            itemsIndexed(
                items = songs,
                key = { _, song -> song.song.id }
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSize = thumbnailSize,
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayAtIndex(
                            songs.map(DetailedSong::asMediaItem),
                            index
                        )
                    },
                    menuContent = {
                        InHistoryMediaItemMenu(song = song)
                    },
                    onThumbnailContent = {
                        AnimatedVisibility(
                            visible = sortBy == SongSortBy.PlayTime,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        ) {
                            BasicText(
                                text = song.song.formattedTotalPlayTime,
                                style = typography.xxs.semiBold.center.color(
                                    Color.White
                                ),
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
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
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

@ExperimentalFoundationApi
@Composable
fun PlaylistsTab(
    onBuiltInPlaylistClicked: (BuiltInPlaylist) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isCachedPlaylistShown by rememberPreference(isCachedPlaylistShownKey, false)
    var sortBy by rememberPreference("playlistSortBy", PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference("playlistSortOrder", SortOrder.Ascending)

    val playlistPreviews by remember(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

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

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorPalette.background,
                            Color.Transparent
                        )
                    )
                )
                .fillMaxWidth()
                .zIndex(1f)
                .padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable {
                        isCreatingANewPlaylist = true
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
                    @Composable
                    fun Item(
                        text: String,
                        textColor: Color,
                        backgroundColor: Color,
                        onClick: () -> Unit
                    ) {
                        BasicText(
                            text = text,
                            style = typography.xxs.medium.copy(
                                color = textColor,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = rememberRipple(
                                        bounded = true
                                    ),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        isSortMenuDisplayed = false
                                        onClick()
                                    }
                                )
                                .background(backgroundColor)
                                .fillMaxWidth()
                                .widthIn(min = 124.dp, max = 248.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                        )
                    }

                    @Composable
                    fun Item(
                        text: String,
                        isSelected: Boolean,
                        onClick: () -> Unit
                    ) {
                        Item(
                            text = text,
                            textColor = if (isSelected) {
                                colorPalette.onPrimaryContainer
                            } else {
                                colorPalette.textSecondary
                            },
                            backgroundColor = if (isSelected) {
                                colorPalette.primaryContainer
                            } else {
                                colorPalette.elevatedBackground
                            },
                            onClick = onClick
                        )
                    }

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = "NAME",
                            isSelected = sortBy == PlaylistSortBy.Name,
                            onClick = {
                                sortBy = PlaylistSortBy.Name
                            }
                        )
                        Item(
                            text = "DATE ADDED",
                            isSelected = sortBy == PlaylistSortBy.DateAdded,
                            onClick = {
                                sortBy = PlaylistSortBy.DateAdded
                            }
                        )
                        Item(
                            text = "SONG COUNT",
                            isSelected = sortBy == PlaylistSortBy.SongCount,
                            onClick = {
                                sortBy = PlaylistSortBy.SongCount
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = when (sortOrder) {
                                SortOrder.Ascending -> "ASCENDING"
                                SortOrder.Descending -> "DESCENDING"
                            },
                            textColor = colorPalette.text,
                            backgroundColor = colorPalette.elevatedBackground,
                            onClick = {
                                sortOrder = !sortOrder
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(top = 36.dp)) {
            item(key = "favorites") {
                BuiltInPlaylistItem(
                    icon = R.drawable.heart,
                    colorTint = colorPalette.red,
                    name = "Favorites",
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onBuiltInPlaylistClicked(BuiltInPlaylist.Favorites) }
                        )
                )
            }

            if (isCachedPlaylistShown) {
                item(key = "cached") {
                    BuiltInPlaylistItem(
                        icon = R.drawable.download,
                        colorTint = colorPalette.blue,
                        name = "Cached",
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(bounded = true),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onBuiltInPlaylistClicked(BuiltInPlaylist.Cached) }
                            )
                            .animateItemPlacement()
                    )
                }
            }
            items(
                items = playlistPreviews,
                key = { it.playlist.id }
            ) { playlistPreview ->
                PlaylistPreviewItem(
                    playlistPreview = playlistPreview,
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onPlaylistClicked(playlistPreview.playlist) }
                        )
                        .animateItemPlacement()
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun ArtistsTab(
    onArtistClicked: (Artist) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("artistSortBy", ArtistSortBy.Name)
    var sortOrder by rememberPreference("artistSortOrder", SortOrder.Ascending)

    val artists by remember(sortBy, sortOrder) {
        Database.artists(sortBy, sortOrder).distinctUntilChanged()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    val thumbnailSizePx = with(LocalDensity.current) { Dimensions.thumbnails.song.roundToPx() }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorPalette.background,
                            Color.Transparent
                        )
                    )
                )
                .fillMaxWidth()
                .zIndex(1f)
                .padding(horizontal = 8.dp)
        ) {
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
                    @Composable
                    fun Item(
                        text: String,
                        textColor: Color,
                        backgroundColor: Color,
                        onClick: () -> Unit
                    ) {
                        BasicText(
                            text = text,
                            style = typography.xxs.medium.copy(
                                color = textColor,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = rememberRipple(
                                        bounded = true
                                    ),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        isSortMenuDisplayed = false
                                        onClick()
                                    }
                                )
                                .background(backgroundColor)
                                .fillMaxWidth()
                                .widthIn(min = 124.dp, max = 248.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                        )
                    }

                    @Composable
                    fun Item(
                        text: String,
                        isSelected: Boolean,
                        onClick: () -> Unit
                    ) {
                        Item(
                            text = text,
                            textColor = if (isSelected) {
                                colorPalette.onPrimaryContainer
                            } else {
                                colorPalette.textSecondary
                            },
                            backgroundColor = if (isSelected) {
                                colorPalette.primaryContainer
                            } else {
                                colorPalette.elevatedBackground
                            },
                            onClick = onClick
                        )
                    }

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = "NAME",
                            isSelected = sortBy == ArtistSortBy.Name,
                            onClick = {
                                sortBy = ArtistSortBy.Name
                            }
                        )
                        Item(
                            text = "DATE ADDED",
                            isSelected = sortBy == ArtistSortBy.DateAdded,
                            onClick = {
                                sortBy = ArtistSortBy.DateAdded
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = when (sortOrder) {
                                SortOrder.Ascending -> "ASCENDING"
                                SortOrder.Descending -> "DESCENDING"
                            },
                            textColor = colorPalette.text,
                            backgroundColor = colorPalette.elevatedBackground,
                            onClick = {
                                sortOrder = !sortOrder
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 36.dp),
//            reverseLayout = when (sortOrder) {
//                SortOrder.Ascending -> false
//                SortOrder.Descending -> true
//            }
        ) {
            items(
                items = artists,
                key = Artist::id
            ) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onArtistClicked(artist) }
                        )
                        .animateItemPlacement()
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = artist.thumbnailUrl?.thumbnail(thumbnailSizePx),
                        contentDescription = null,
                        error = {
                            Box {
                                Image(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(24.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(Dimensions.thumbnails.song)
                    )

                    BasicText(
                        text = artist.name,
                        style = typography.xs.semiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun AlbumsTab(
    onAlbumClicked: (Album) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("albumSortBy", AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference("albumSortOrder", SortOrder.Descending)

    val albums by remember(sortBy, sortOrder) {
        Database.albums(sortBy, sortOrder).distinctUntilChanged()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    val thumbnailSizePx = with(LocalDensity.current) { Dimensions.thumbnails.song.roundToPx() }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorPalette.background,
                            Color.Transparent
                        )
                    )
                )
                .fillMaxWidth()
                .zIndex(1f)
                .padding(horizontal = 8.dp)
        ) {
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
                    @Composable
                    fun Item(
                        text: String,
                        textColor: Color,
                        backgroundColor: Color,
                        onClick: () -> Unit
                    ) {
                        BasicText(
                            text = text,
                            style = typography.xxs.medium.copy(
                                color = textColor,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = rememberRipple(
                                        bounded = true
                                    ),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        isSortMenuDisplayed = false
                                        onClick()
                                    }
                                )
                                .background(backgroundColor)
                                .fillMaxWidth()
                                .widthIn(min = 124.dp, max = 248.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                        )
                    }

                    @Composable
                    fun Item(
                        text: String,
                        isSelected: Boolean,
                        onClick: () -> Unit
                    ) {
                        Item(
                            text = text,
                            textColor = if (isSelected) {
                                colorPalette.onPrimaryContainer
                            } else {
                                colorPalette.textSecondary
                            },
                            backgroundColor = if (isSelected) {
                                colorPalette.primaryContainer
                            } else {
                                colorPalette.elevatedBackground
                            },
                            onClick = onClick
                        )
                    }

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = "NAME",
                            isSelected = sortBy == AlbumSortBy.Title,
                            onClick = {
                                sortBy = AlbumSortBy.Title
                            }
                        )

                        Item(
                            text = "YEAR",
                            isSelected = sortBy == AlbumSortBy.Year,
                            onClick = {
                                sortBy = AlbumSortBy.Year
                            }
                        )

                        Item(
                            text = "DATE ADDED",
                            isSelected = sortBy == AlbumSortBy.DateAdded,
                            onClick = {
                                sortBy = AlbumSortBy.DateAdded
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colorPalette.elevatedBackground)
                            .width(IntrinsicSize.Max),
                    ) {
                        Item(
                            text = when (sortOrder) {
                                SortOrder.Ascending -> "ASCENDING"
                                SortOrder.Descending -> "DESCENDING"
                            },
                            textColor = colorPalette.text,
                            backgroundColor = colorPalette.elevatedBackground,
                            onClick = {
                                sortOrder = !sortOrder
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(top = 36.dp)) {
            items(
                items = albums,
                key = Album::id
            ) { album ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onAlbumClicked(album) }
                        )
                        .animateItemPlacement()
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = album.thumbnailUrl?.thumbnail(thumbnailSizePx),
                        contentDescription = null,
                        error = {
                            Box {
                                Image(
                                    painter = painterResource(R.drawable.disc),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(ThumbnailRoundness.shape)
                            .size(Dimensions.thumbnails.song)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        BasicText(
                            text = album.title ?: "Unknown",
                            style = typography.xs.semiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        album.authorsText?.let {
                            BasicText(
                                text = album.authorsText,
                                style = typography.xs.semiBold.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    album.year?.let {
                        BasicText(
                            text = album.year,
                            style = typography.xxs.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}