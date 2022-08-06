package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.ChipGroup
import it.vfsfitvnm.vimusic.ui.components.ChipItem
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.relaunchableEffect
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.searchFilterKey
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    var searchFilter by rememberPreference(searchFilterKey, YouTube.Item.Song.Filter.value)

    val lazyListState = rememberLazyListState()

    val items = remember(searchFilter) {
        mutableStateListOf<YouTube.Item>()
    }

    var continuationResult by remember(searchFilter) {
        mutableStateOf<Result<String?>?>(null)
    }

    val onLoad = relaunchableEffect(searchFilter) {
        withContext(Dispatchers.Main) {
            val token = continuationResult?.getOrNull()

            continuationResult = null

            continuationResult = withContext(Dispatchers.IO) {
                YouTube.search(query, searchFilter, token)
            }?.map { searchResult ->
                items.addAll(searchResult.items)
                searchResult.continuation
            }
        }
    }

    val thumbnailSizePx = Dimensions.thumbnails.song.px

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            AlbumScreen(
                browseId = browseId ?: "browseId cannot be null"
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: "browseId cannot be null"
            )
        }

        playlistRoute { browseId ->
            PlaylistScreen(
                browseId = browseId ?: "browseId cannot be null"
            )
        }

        host {
            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = Dimensions.collapsedPlayer),
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
                            painter = painterResource(R.drawable.chevron_back),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable(onClick = pop)
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )

                        BasicText(
                            text = query,
                            style = typography.m.semiBold.center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onSearchAgain
                                )
                                .weight(1f)
                        )

                        Spacer(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
                    ChipGroup(
                        items = listOf(
                            ChipItem(
                                text = stringResource(R.string.songs),
                                value = YouTube.Item.Song.Filter.value
                            ),
                            ChipItem(
                                text = stringResource(R.string.albums),
                                value = YouTube.Item.Album.Filter.value
                            ),
                            ChipItem(
                                text = stringResource(R.string.artists),
                                value = YouTube.Item.Artist.Filter.value
                            ),
                            ChipItem(
                                text = stringResource(R.string.videos),
                                value = YouTube.Item.Video.Filter.value
                            ),
                            ChipItem(
                                text = stringResource(R.string.playlists),
                                value = YouTube.Item.CommunityPlaylist.Filter.value
                            ),
                            ChipItem(
                                text = stringResource(R.string.featured_playlists),
                                value = YouTube.Item.FeaturedPlaylist.Filter.value
                            ),
                        ),
                        value = searchFilter,
                        selectedBackgroundColor = colorPalette.primaryContainer,
                        unselectedBackgroundColor = colorPalette.lightBackground,
                        selectedTextStyle = typography.xs.medium.color(colorPalette.onPrimaryContainer),
                        unselectedTextStyle = typography.xs.medium,
                        shape = RoundedCornerShape(36.dp),
                        onValueChanged = {
                            searchFilter = it
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                items(
                    items = items,
                    contentType = { it }
                ) { item ->
                    SmallItem(
                        item = item,
                        thumbnailSizeDp = Dimensions.thumbnails.song,
                        thumbnailSizePx = thumbnailSizePx,
                        onClick = {
                            when (item) {
                                is YouTube.Item.Album -> albumRoute(item.info.endpoint!!.browseId)
                                is YouTube.Item.Artist -> artistRoute(item.info.endpoint!!.browseId)
                                is YouTube.Item.Playlist -> playlistRoute(item.info.endpoint!!.browseId)
                                is YouTube.Item.Song -> {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(item.asMediaItem)
                                    binder?.setupRadio(item.info.endpoint)
                                }
                                is YouTube.Item.Video -> {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(item.asMediaItem)
                                    binder?.setupRadio(item.info.endpoint)
                                }
                            }
                        }
                    )
                }

                continuationResult?.getOrNull()?.let {
                    if (items.isNotEmpty()) {
                        item {
                            SideEffect(onLoad)
                        }
                    }
                } ?: continuationResult?.exceptionOrNull()?.let { throwable ->
                    item {
                        LoadingOrError(
                            errorMessage = throwable.javaClass.canonicalName,
                            onRetry = onLoad
                        )
                    }
                } ?: continuationResult?.let {
                    if (items.isEmpty()) {
                        item {
                            TextCard(icon = R.drawable.sad) {
                                Title(text = stringResource(R.string.no_results))
                                Text(text = stringResource(R.string.other_category))
                            }
                        }
                    }
                } ?: item(key = "loading") {
                    LoadingOrError(
                        itemCount = if (items.isEmpty()) 8 else 3,
                        isLoadingArtists = searchFilter == YouTube.Item.Artist.Filter.value
                    )
                }
            }
        }
    }
}

@Composable
fun SmallSongItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.darkGray, shape = ThumbnailRoundness.shape)
                .size(thumbnailSizeDp)
        )

        Column {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

@Composable
fun SmallArtistItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.darkGray, shape = CircleShape)
                .size(thumbnailSizeDp)
        )

        TextPlaceholder()
    }
}

@ExperimentalAnimationApi
@Composable
fun SmallItem(
    item: YouTube.Item,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item) {
        is YouTube.Item.Artist -> SmallArtistItem(
            artist = item,
            thumbnailSizeDp = thumbnailSizeDp,
            thumbnailSizePx = thumbnailSizePx,
            modifier = modifier
                .clickable(
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
        )
        is YouTube.Item.Song -> SmallSongItem(
            song = item,
            thumbnailSizePx = thumbnailSizePx,
            onClick = onClick,
            modifier = modifier
        )
        is YouTube.Item.Album -> SmallAlbumItem(
            album = item,
            thumbnailSizeDp = thumbnailSizeDp,
            thumbnailSizePx = thumbnailSizePx,
            modifier = modifier
                .clickable(
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
        )
        is YouTube.Item.Video -> SmallVideoItem(
            video = item,
            thumbnailSizePx = thumbnailSizePx,
            onClick = onClick,
            modifier = modifier
        )
        is YouTube.Item.Playlist -> SmallPlaylistItem(
            playlist = item,
            thumbnailSizeDp = thumbnailSizeDp,
            thumbnailSizePx = thumbnailSizePx,
            modifier = modifier
                .clickable(
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun SmallSongItem(
    song: YouTube.Item.Song,
    thumbnailSizePx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SongItem(
        thumbnailModel = song.thumbnail?.size(thumbnailSizePx),
        title = song.info.name,
        authors = song.authors.joinToString("") { it.name },
        durationText = song.durationText,
        onClick = onClick,
        menuContent = {
            NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
        },
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun SmallVideoItem(
    video: YouTube.Item.Video,
    thumbnailSizePx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SongItem(
        thumbnailModel = video.thumbnail?.size(thumbnailSizePx),
        title = video.info.name,
        authors = (if (video.isOfficialMusicVideo) video.authors else video.views)
            .joinToString("") { it.name },
        durationText = video.durationText,
        onClick = onClick,
        menuContent = {
            NonQueuedMediaItemMenu(mediaItem = video.asMediaItem)
        },
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun SmallPlaylistItem(
    playlist: YouTube.Item.Playlist,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier
) {
    val (_, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AsyncImage(
            model = playlist.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(ThumbnailRoundness.shape)
                .size(thumbnailSizeDp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            BasicText(
                text = playlist.info.name,
                style = typography.xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = playlist.channel?.name ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        playlist.songCount?.let { songCount ->
            BasicText(
                text = "$songCount" + stringResource(R.string.songs),
                style = typography.xxs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SmallAlbumItem(
    album: YouTube.Item.Album,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AsyncImage(
            model = album.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(ThumbnailRoundness.shape)
                .size(thumbnailSizeDp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            BasicText(
                text = album.info.name,
                style = typography.xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = album.authors?.joinToString("") { it.name } ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        album.year?.let { year ->
            BasicText(
                text = year,
                style = typography.xxs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SmallArtistItem(
    artist: YouTube.Item.Artist,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AsyncImage(
            model = artist.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(thumbnailSizeDp)
        )

        BasicText(
            text = artist.info.name,
            style = typography.xs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun LoadingOrError(
    itemCount: Int = 0,
    isLoadingArtists: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    LoadingOrError(
        errorMessage = errorMessage,
        onRetry = onRetry,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(itemCount) { index ->
            if (isLoadingArtists) {
                SmallArtistItemShimmer(
                    thumbnailSizeDp = Dimensions.thumbnails.song,
                    modifier = Modifier
                        .alpha(1f - index * 0.125f)
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                )
            } else {
                SmallSongItemShimmer(
                    thumbnailSizeDp = Dimensions.thumbnails.song,
                    modifier = Modifier
                        .alpha(1f - index * 0.125f)
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                )
            }
        }
    }
}
