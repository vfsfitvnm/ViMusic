package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class Appearance(
    val colorPalette: ColorPalette,
    val typography: Typography,
    val thumbnailShape: Shape,
) {
    companion object : Saver<Appearance, List<Any>> {
        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any>): Appearance {
            return Appearance(
                colorPalette = ColorPalette.restore(value[0] as List<Any>),
                typography = Typography.restore(value[1] as List<Any>),
                thumbnailShape = RoundedCornerShape((value[2] as Int).dp)
            )
        }

        override fun SaverScope.save(value: Appearance) =
            listOf(
                with (ColorPalette.Companion) { save(value.colorPalette) },
                with (Typography.Companion) { save(value.typography) },
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
