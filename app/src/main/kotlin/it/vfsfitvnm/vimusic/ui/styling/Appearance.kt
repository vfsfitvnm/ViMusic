package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape

data class Appearance(
    val colorPalette: ColorPalette,
    val typography: Typography,
    val thumbnailShape: Shape
)

val LocalAppearance = staticCompositionLocalOf<Appearance> { TODO() }
