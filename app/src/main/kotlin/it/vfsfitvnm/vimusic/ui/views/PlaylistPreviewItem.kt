package it.vfsfitvnm.vimusic.ui.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt


@Composable
fun PlaylistPreviewItem(
    playlistPreview: PlaylistPreview,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val density = LocalDensity.current

    val thumbnailSizePx = density.run {
        thumbnailSize.toPx().roundToInt()
    }

    val thumbnails by remember(playlistPreview.playlist.id) {
        Database.playlistThumbnailUrls(playlistPreview.playlist.id).distinctUntilChanged()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    PlaylistItem(
        name = playlistPreview.playlist.name,
        modifier = modifier,
        thumbnailSize = thumbnailSize,
        trailingContent = trailingContent,
        songCount = playlistPreview.songCount,
        imageContent = {
            if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = thumbnails.first().thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(thumbnailSize)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(thumbnailSize)
                ) {
                    listOf(
                        Alignment.TopStart,
                        Alignment.TopEnd,
                        Alignment.BottomStart,
                        Alignment.BottomEnd
                    ).forEachIndexed { index, alignment ->
                        AsyncImage(
                            model = thumbnails.getOrNull(index).thumbnail(thumbnailSizePx),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .align(alignment)
                                .size(thumbnailSize / 2)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun BuiltInPlaylistItem(
    @DrawableRes icon: Int,
    colorTint: Color,
    name: String,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song
) {
    PlaylistItem(
        name = name,
        modifier = modifier,
        thumbnailSize = thumbnailSize,
        songCount = null,
        imageContent = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorTint),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(18.dp)
            )
        }
    )
}

@Composable
fun PlaylistItem(
    name: String,
    songCount: Int?,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song,
    trailingContent: (@Composable () -> Unit)? = null,
    imageContent: @Composable BoxScope.() -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .padding(start = 16.dp, end = if (trailingContent == null) 16.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(ThumbnailRoundness.shape)
                .background(colorPalette.lightBackground)
                .size(thumbnailSize),
            content = imageContent
        )

        BasicText(
            text = name,
            style = typography.xs.semiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
        )

        songCount?.let {
            BasicText(
                text = "$songCount song${if (songCount == 1) "" else "s"}",
                style = typography.xxs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        trailingContent?.invoke()
    }
}

