package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
fun PlaylistPreviewItem(
    playlistPreview: PlaylistPreview,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = Dimensions.thumbnails.song,
) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current
    val density = LocalDensity.current

    val thumbnailSizePx = density.run {
        thumbnailSize.toPx().toInt()
    }

    val thumbnails by remember(playlistPreview.playlist.id) {
        Database.playlistThumbnailUrls(playlistPreview.playlist.id).distinctUntilChanged()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    Box(
        modifier = modifier
            .background(colorPalette.lightBackground)
            .size(thumbnailSize * 2)
    ) {
        if (thumbnails.toSet().size == 1) {
            AsyncImage(
                model = thumbnails.first().thumbnail(thumbnailSizePx * 2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(thumbnailSize * 2)
            )
        } else {
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
                        .size(thumbnailSize)
                )
            }
        }

        BasicText(
            text = playlistPreview.playlist.name,
            style = typography.xxs.semiBold.color(Color.White),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}