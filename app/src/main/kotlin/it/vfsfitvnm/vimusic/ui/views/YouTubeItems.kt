package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.youtubemusic.YouTube

@Composable
fun SmallSongItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = Dimensions.itemsVerticalPadding)
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        Column {
            TextPlaceholder()
            TextPlaceholder()
        }
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun VideoItem(
    video: YouTube.Item.Video,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuState = LocalMenuState.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .combinedClickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onLongClick = {
                    menuState.display {
                        NonQueuedMediaItemMenu(mediaItem = video.asMediaItem)
                    }
                },
                onClick = onClick
            )
            .fillMaxWidth()
            .padding(vertical = Dimensions.itemsVerticalPadding)
            .padding(horizontal = 16.dp)
    ) {
        Box {
            AsyncImage(
                model = video.thumbnail?.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape)
                    .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
            )

            video.durationText?.let { durationText ->
                BasicText(
                    text = durationText,
                    style = typography.xxs.medium.color(colorPalette.onOverlay),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .background(color = colorPalette.overlay, shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Column {
            BasicText(
                text = video.info.name,
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            BasicText(
                text = video.authors.joinToString("") { it.name },
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            video.views.firstOrNull()?.name?.let { viewsText ->
                BasicText(
                    text = viewsText,
                    style = typography.xxs.medium.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun VideoItemShimmer(
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = Dimensions.itemsVerticalPadding)
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
        )

        Column {
            TextPlaceholder()
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: YouTube.Item.Playlist,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = playlist.thumbnail?.size(thumbnailSizePx),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape)
                    .size(thumbnailSizeDp)
            )

            playlist.songCount?.let { songCount ->
                BasicText(
                    text = "$songCount",
                    style = typography.xxs.medium.color(colorPalette.onOverlay),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .background(color = colorPalette.overlay, shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = playlist.info.name,
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            BasicText(
                text = playlist.channel?.name ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PlaylistItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

@Composable
fun AlbumItem(
    album: YouTube.Item.Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (_, typography, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = album.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(thumbnailShape)
                .size(thumbnailSizeDp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = album.info.name,
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            BasicText(
                text = album.authors?.joinToString("") { it.name } ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            album.year?.let { year ->
                BasicText(
                    text = year,
                    style = typography.xxs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextPlaceholder()
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: YouTube.Item.Artist,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = artist.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(thumbnailSizeDp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = artist.info.name,
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            artist.subscribersCountText?.let { subscribersCountText ->
                BasicText(
                    text = subscribersCountText,
                    style = typography.xxs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistItemShimmer(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val (colorPalette) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = CircleShape)
                .size(thumbnailSizeDp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SearchResultLoadingOrError(
    itemCount: Int = 0,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    shimmerContent: @Composable BoxScope.() -> Unit,
) {
    LoadingOrError(
        errorMessage = errorMessage,
        onRetry = onRetry,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(itemCount) { index ->
            Box(
                modifier = Modifier
                    .alpha(1f - index * 0.125f),
                content = shimmerContent
            )
//            if (isLoadingArtists) {
//                SmallArtistItemShimmer(
//                    thumbnailSizeDp = Dimensions.thumbnails.song,
//                    modifier = Modifier
//                        .alpha(1f - index * 0.125f)
//                        .fillMaxWidth()
//                        .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
//                )
//            } else {
//                SmallSongItemShimmer(
//                    thumbnailSizeDp = Dimensions.thumbnails.song,
//                    modifier = Modifier
//                        .alpha(1f - index * 0.125f)
//                        .fillMaxWidth()
//                        .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
//                )
//            }
        }
    }
}
