package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.*
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(
    query: String,
    onSearchAgain: () -> Unit,
) {
    val density = LocalDensity.current
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current
    val preferences = LocalPreferences.current
    val player = LocalYoutubePlayer.current

    val lazyListState = rememberLazyListState()

    var continuation by remember(preferences.searchFilter) {
        mutableStateOf<Outcome<String?>>(Outcome.Initial)
    }

    val items = remember(preferences.searchFilter) {
        mutableStateListOf<YouTube.Item>()
    }

    val onLoad = relaunchableEffect(preferences.searchFilter) {
        withContext(Dispatchers.Main) {
            val token = continuation.valueOrNull

            continuation = Outcome.Loading

            continuation = withContext(Dispatchers.IO) {
                YouTube.search(query, preferences.searchFilter, token)
            }.map { searchResult ->
                items.addAll(searchResult.items)
                searchResult.continuation
            }.recoverWith(token)
        }
    }

    val thumbnailSizePx = remember {
        density.run {
            54.dp.roundToPx()
        }
    }

    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    RouteHandler(
        listenToGlobalEmitter = true
    ) {
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

        host {
            val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 64.dp),
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
                                text = "Songs",
                                value = YouTube.Item.Song.Filter.value
                            ),
                            ChipItem(
                                text = "Albums",
                                value = YouTube.Item.Album.Filter.value
                            ),
                            ChipItem(
                                text = "Artists",
                                value = YouTube.Item.Artist.Filter.value
                            ),
                            ChipItem(
                                text = "Videos",
                                value = YouTube.Item.Video.Filter.value
                            ),
                        ),
                        value = preferences.searchFilter,
                        selectedBackgroundColor = colorPalette.primaryContainer,
                        unselectedBackgroundColor = colorPalette.lightBackground,
                        selectedTextStyle = typography.xs.medium.color(colorPalette.onPrimaryContainer),
                        unselectedTextStyle = typography.xs.medium,
                        shape = RoundedCornerShape(36.dp),
                        onValueChanged = preferences.onSearchFilterChange,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                items(items) { item ->
                    SmallItem(
                        item = item,
                        thumbnailSizeDp = 54.dp,
                        thumbnailSizePx = thumbnailSizePx,
                        onClick = {
                            when (item) {
                                is YouTube.Item.Album -> albumRoute(item.info.endpoint!!.browseId)
                                is YouTube.Item.Artist -> artistRoute(item.info.endpoint!!.browseId)
                                is YouTube.Item.Song -> {
                                    player?.mediaController?.forcePlay(item.asMediaItem)
                                    item.info.endpoint?.let(YoutubePlayer.Radio::setup)
                                }
                                is YouTube.Item.Video -> {
                                    player?.mediaController?.forcePlay(item.asMediaItem)
                                    item.info.endpoint?.let(YoutubePlayer.Radio::setup)
                                }
                            }
                        }
                    )
                }

                when (val currentResult = continuation) {
                    is Outcome.Error -> item {
                        Error(
                            error = currentResult,
                            onRetry = onLoad,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                    is Outcome.Recovered -> item {
                        Error(
                            error = currentResult.error,
                            onRetry = onLoad,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                    is Outcome.Success -> {
                        if (items.isEmpty()) {
                            item {
                                Message(
                                    text = "No results found",
                                    modifier = Modifier
                                )
                            }
                        }

                        if (currentResult.value != null) {
                            item {
                                SideEffect(onLoad)
                            }
                        }
                    }
                    else -> {}
                }

                if (continuation is Outcome.Loading || (continuation is Outcome.Success && continuation.valueOrNull != null)) {
                    items(count = if (items.isEmpty()) 8 else 3, key = { it }) { index ->
                        when (preferences.searchFilter) {
                            YouTube.Item.Artist.Filter.value -> SmallArtistItemShimmer(
                                shimmer = shimmer,
                                thumbnailSizeDp = 54.dp,
                                modifier = Modifier
                                    .alpha(1f - index * 0.125f)
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 16.dp)
                            )
                            else -> SmallSongItemShimmer(
                                shimmer = shimmer,
                                thumbnailSizeDp = 54.dp,
                                modifier = Modifier
                                    .alpha(1f - index * 0.125f)
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallSongItemShimmer(
    shimmer: Shimmer,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val colorPalette = LocalColorPalette.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .shimmer(shimmer)
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
    shimmer: Shimmer,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val colorPalette = LocalColorPalette.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .shimmer(shimmer)
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
                .padding(vertical = 4.dp, horizontal = 16.dp)
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
                .padding(vertical = 4.dp, horizontal = 16.dp)
        )
        is YouTube.Item.Video -> SmallVideoItem(
            video = item,
            thumbnailSizePx = thumbnailSizePx,
            onClick = onClick,
            modifier = modifier
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
        thumbnailModel = song.thumbnail.size(thumbnailSizePx),
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
        thumbnailModel = video.thumbnail.size(thumbnailSizePx),
        title = video.info.name,
        authors = video.views.joinToString("") { it.name },
        durationText = video.durationText,
        onClick = onClick,
        menuContent = {
            NonQueuedMediaItemMenu(mediaItem = video.asMediaItem)
        },
        modifier = modifier
    )
}

@Composable
fun SmallAlbumItem(
    album: YouTube.Item.Album,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
) {
    val typography = LocalTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AsyncImage(
            model = album.thumbnail.size(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
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
                text = "${album.authors.joinToString("") { it.name }} • ${album.year}",
                style = typography.xs,
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
    val typography = LocalTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AsyncImage(
            model = artist.thumbnail.size(thumbnailSizePx),
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