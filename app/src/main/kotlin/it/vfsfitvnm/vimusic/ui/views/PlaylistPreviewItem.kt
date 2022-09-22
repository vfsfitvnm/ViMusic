package it.vfsfitvnm.vimusic.ui.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
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
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun PlaylistPreviewItem(
    playlistPreview: PlaylistPreview,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song
) {
    val density = LocalDensity.current
    val (_, _, thumbnailShape) = LocalAppearance.current

    val thumbnailSizePx = with(density) {
        thumbnailSize.roundToPx()
    }

    val thumbnails by remember(playlistPreview.playlist.id) {
        Database.playlistThumbnailUrls(playlistPreview.playlist.id).distinctUntilChanged().map {
            it.map { url ->
                url.thumbnail(thumbnailSizePx)
            }
        }
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    PlaylistItem(
        name = playlistPreview.playlist.name,
        thumbnailSize = thumbnailSize,
        imageContent = {
            if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = thumbnails.first().thumbnail(thumbnailSizePx * 2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    listOf(
                        Alignment.TopStart,
                        Alignment.TopEnd,
                        Alignment.BottomStart,
                        Alignment.BottomEnd
                    ).forEachIndexed { index, alignment ->
                        AsyncImage(
                            model = thumbnails.getOrNull(index),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .align(alignment)
                                .size(thumbnailSize)
                        )
                    }
                }
            }
        },
        modifier = modifier
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
        thumbnailSize = thumbnailSize,
        imageContent = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorTint),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        },
        modifier = modifier,
    )
}

@Composable
fun PlaylistItem(
    name: String,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song,
    imageContent: @Composable BoxScope.() -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .requiredWidth(thumbnailSize * 2)
    ) {
        Box(
            modifier = Modifier
                .clip(thumbnailShape)
                .background(colorPalette.background1)
                .align(Alignment.CenterHorizontally)
                .requiredSize(thumbnailSize * 2),
            content = imageContent
        )

        BasicText(
            text = name,
            style = typography.xxs.semiBold.center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
