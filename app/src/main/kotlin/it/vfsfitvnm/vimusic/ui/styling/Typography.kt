package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.vfsfitvnm.vimusic.R

@Immutable
data class Typography(
    val xxs: TextStyle,
    val xs: TextStyle,
    val s: TextStyle,
    val m: TextStyle,
    val l: TextStyle,
    val xxl: TextStyle,
) {
    fun copy(color: Color) = Typography(
        xxs = xxs.copy(color = color),
        xs = xs.copy(color = color),
        s = s.copy(color = color),
        m = m.copy(color = color),
        l = l.copy(color = color),
        xxl = xxl.copy(color = color)
    )

    companion object : Saver<Typography, List<Any>> {
        override fun restore(value: List<Any>) = typographyOf(
            Color((value[0] as Long).toULong()),
            value[1] as Boolean,
            value[2] as Boolean
        )

        override fun SaverScope.save(value: Typography) =
            listOf(
                value.xxs.color.value.toLong(),
                value.xxs.fontFamily == FontFamily.Default,
                value.xxs.platformStyle?.paragraphStyle?.includeFontPadding ?: false
            )
    }
}

fun typographyOf(color: Color, useSystemFont: Boolean, applyFontPadding: Boolean): Typography {
    val textStyle = TextStyle(
        fontFamily = if (useSystemFont) {
            FontFamily.Default
        } else {
            FontFamily(
                Font(
                    resId = R.font.poppins_w300,
                    weight = FontWeight.Light
                ),
                Font(
                    resId = R.font.poppins_w400,
                    weight = FontWeight.Normal
                ),
                Font(
                    resId = R.font.poppins_w500,
                    weight = FontWeight.Medium
                ),
                Font(
                    resId = R.font.poppins_w600,
                    weight = FontWeight.SemiBold
                ),
                Font(
                    resId = R.font.poppins_w700,
                    weight = FontWeight.Bold
                ),
            )
        },
        fontWeight = FontWeight.Normal,
        color = color,
        platformStyle = @Suppress("DEPRECATION") (PlatformTextStyle(includeFontPadding = applyFontPadding))
    )

    return Typography(
        xxs = textStyle.copy(fontSize = 12.sp),
        xs = textStyle.copy(fontSize = 14.sp),
        s = textStyle.copy(fontSize = 16.sp),
        m = textStyle.copy(fontSize = 18.sp),
        l = textStyle.copy(fontSize = 20.sp),
        xxl = textStyle.copy(fontSize = 32.sp)
    )
}
