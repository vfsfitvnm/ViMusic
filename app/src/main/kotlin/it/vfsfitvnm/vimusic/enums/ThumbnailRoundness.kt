package it.vfsfitvnm.vimusic.enums

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.utils.LocalPreferences

enum class ThumbnailRoundness {
    None,
    Light,
    Medium,
    Heavy;

    companion object {
        val shape: Shape
            @Composable
            @ReadOnlyComposable
            get() = when (LocalPreferences.current.thumbnailRoundness) {
                None -> RectangleShape
                Light -> RoundedCornerShape(2.dp)
                Medium -> RoundedCornerShape(4.dp)
                Heavy -> RoundedCornerShape(8.dp)
            }
    }
}
