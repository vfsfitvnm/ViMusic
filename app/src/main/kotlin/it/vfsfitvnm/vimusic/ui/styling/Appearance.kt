package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class Appearance(
    val colorPalette: ColorPalette,
    val typography: Typography = typographyOf(colorPalette.text),
    val thumbnailShape: Shape
) {
    companion object : Saver<Appearance, List<Any>> {
        override fun restore(value: List<Any>) = Appearance(
            colorPalette = ColorPalette(
                background0 = Color((value[0] as Long).toULong()),
                background1 = Color((value[1] as Long).toULong()),
                background2 = Color((value[2] as Long).toULong()),
                accent = Color((value[3] as Long).toULong()),
                onAccent = Color((value[4] as Long).toULong()),
                red = Color((value[5] as Long).toULong()),
                blue = Color((value[6] as Long).toULong()),
                text = Color((value[7] as Long).toULong()),
                textSecondary = Color((value[8] as Long).toULong()),
                textDisabled = Color((value[9] as Long).toULong()),
                isDark = value[10] as Boolean
            ),
            thumbnailShape = RoundedCornerShape((value[11] as Int).dp)
        )

        override fun SaverScope.save(value: Appearance) =
            listOf(
                value.colorPalette.background0.value.toLong(),
                value.colorPalette.background1.value.toLong(),
                value.colorPalette.background2.value.toLong(),
                value.colorPalette.accent.value.toLong(),
                value.colorPalette.onAccent.value.toLong(),
                value.colorPalette.red.value.toLong(),
                value.colorPalette.blue.value.toLong(),
                value.colorPalette.text.value.toLong(),
                value.colorPalette.textSecondary.value.toLong(),
                value.colorPalette.textDisabled.value.toLong(),
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
