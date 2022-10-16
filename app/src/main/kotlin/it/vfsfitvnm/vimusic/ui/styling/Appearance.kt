package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils

data class Appearance(
    val colorPalette: ColorPalette,
    val typography: Typography,
    val thumbnailShape: Shape
) {
    companion object : Saver<Appearance, List<Any>> {
        override fun restore(value: List<Any>): Appearance {
            val colorPalette = when (val accent = value[0] as Int) {
                0 -> DefaultDarkColorPalette
                1 -> DefaultLightColorPalette
                2 -> PureBlackColorPalette
                else -> dynamicColorPaletteOf(
                    FloatArray(3).apply { ColorUtils.colorToHSL(accent, this) },
                    value[1] as Boolean
                )
            }

            return Appearance(
                colorPalette = colorPalette,
                typography = typographyOf(colorPalette.text),
                thumbnailShape = RoundedCornerShape((value[2] as Int).dp)
            )
        }

        override fun SaverScope.save(value: Appearance) =
            listOf(
                when {
                    value.colorPalette === DefaultDarkColorPalette -> 0
                    value.colorPalette === DefaultLightColorPalette -> 1
                    value.colorPalette === PureBlackColorPalette -> 2
                    else -> value.colorPalette.accent.toArgb()
                },
                value.colorPalette.isDark,
                when (value.thumbnailShape) {
                    RoundedCornerShape(2.dp) -> 2
                    RoundedCornerShape(4.dp) -> 4
                    RoundedCornerShape(8.dp) -> 8
                    else -> 0
                }
            )
    }
}

val LocalAppearance = staticCompositionLocalOf<Appearance> { TODO() }
