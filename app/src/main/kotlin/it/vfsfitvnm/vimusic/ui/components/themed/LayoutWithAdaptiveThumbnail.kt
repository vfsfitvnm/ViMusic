package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.isLandscape
import it.vfsfitvnm.vimusic.utils.thumbnail

@Composable
inline fun LayoutWithAdaptiveThumbnail(
    thumbnailContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val isLandscape = isLandscape

    if (isLandscape) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            thumbnailContent()
            content()
        }
    } else {
        content()
    }
}

fun adaptiveThumbnailContent(
    isLoading: Boolean,
    url: String?,
    shape: Shape? = null
): @Composable () -> Unit = {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val thumbnailSizeDp = if (isLandscape) (maxHeight - 128.dp) else (maxWidth - 64.dp)
        val thumbnailSizePx = thumbnailSizeDp.px

        val modifier = Modifier
            .padding(all = 16.dp)
            .clip(shape ?: thumbnailShape)
            .size(thumbnailSizeDp)

        if (isLoading) {
            Spacer(
                modifier = modifier
                    .shimmer()
                    .background(colorPalette.shimmer)
            )
        } else {
            AsyncImage(
                model = url?.thumbnail(thumbnailSizePx),
                contentDescription = null,
                modifier = modifier
            )
        }
    }
}
