package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.asMediaItem
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
                .background(color = colorPalette.shimmer, shape = CircleShape)
                .size(thumbnailSizeDp)
        )

        TextPlaceholder()
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
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
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
                text = "$songCount songs",
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
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
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
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
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
fun SearchResultLoadingOrError(
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
