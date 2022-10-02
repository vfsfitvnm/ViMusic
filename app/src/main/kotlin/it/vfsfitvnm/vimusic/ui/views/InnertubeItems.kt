package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
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
import it.vfsfitvnm.youtubemusic.Innertube

@ExperimentalAnimationApi
@Composable
fun SongItem(
    song: Innertube.SongItem,
    thumbnailSizePx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SongItem(
        thumbnailModel = song.thumbnail?.size(thumbnailSizePx),
        title = song.info?.name,
        authors = song.authors?.joinToString("") { it.name ?: "" },
        durationText = song.durationText,
        onClick = onClick,
        menuContent = {
            NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
        },
        modifier = modifier
    )
}

@Composable
fun SongItemPlaceholder(
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun VideoItem(
    video: Innertube.VideoItem,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuState = LocalMenuState.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
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

        ItemInfoContainer {
            BasicText(
                text = video.info?.name ?: "",
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            BasicText(
                text = video.authors?.joinToString("") { it.name ?: "" } ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            video.viewsText?.let { viewsText ->
                BasicText(
                    text = viewsText,
                    style = typography.xxs.medium.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun VideoItemPlaceholder(
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
        )

        ItemInfoContainer {
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
    playlist: Innertube.PlaylistItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
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

        ItemInfoContainer {
            BasicText(
                text = playlist.info?.name ?: "",
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
fun PlaylistItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}

@Composable
fun AlbumItem(
    album: Innertube.AlbumItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (_, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        AsyncImage(
            model = album.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            BasicText(
                text = album.info?.name ?: "",
                style = typography.xs.semiBold,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!alternative) {
                BasicText(
                    text = album.authors?.joinToString("") { it.name ?: "" } ?: "",
                    style = typography.xs.semiBold.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            BasicText(
                text = album.year ?: "",
                style = typography.xxs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AlbumItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()

            if (!alternative) {
                TextPlaceholder()
            }

            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: Innertube.ArtistItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    val (_, typography) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = artist.thumbnail?.size(thumbnailSizePx),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            BasicText(
                text = artist.info?.name ?: "",
                style = typography.xs.semiBold,
                maxLines = if (alternative) 1 else 2,
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
fun ArtistItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    val (colorPalette) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = CircleShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private inline fun ItemContainer(
    alternative: Boolean,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit
) {
    if (alternative) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                .width(thumbnailSizeDp)
        ) {
            content()
        }
    } else {
        Row(
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private inline fun ItemInfoContainer(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
        content = content
    )
}
