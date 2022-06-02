package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorPalette(
    val background: Color,
    val elevatedBackground: Color,
    val lightBackground: Color,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val lightGray: Color,
    val gray: Color,
    val darkGray: Color,
    val blue: Color,
    val red: Color,
    val green: Color,
    val orange: Color,

    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val iconOnPrimaryContainer: Color,
)

val DarkColorPalette = ColorPalette(
    background = Color(0xff16171d),
    lightBackground = Color(0xff1f2029),
    elevatedBackground = Color(0xff1f2029),
    text = Color(0xffe1e1e2),
    textSecondary = Color(0xffa3a4a6),
    textDisabled = Color(0xff6f6f73),
    lightGray = Color(0xfff8f8f8),
    gray = Color(0xFFE5E5E5),
    darkGray = Color(0xFF838383),
    blue = Color(0xff4046bf),
    red = Color(0xffbf4040),
    green = Color(0xff7fbf40),
    orange = Color(0xffe8820e),

    primaryContainer = Color(0xff4046bf),
    onPrimaryContainer = Color.White,
    iconOnPrimaryContainer = Color.White,
)

val LightColorPalette = ColorPalette(
    background = Color(0xfffdfdfe),
    lightBackground = Color(0xFFf8f8fc),
    elevatedBackground = Color(0xfffdfdfe),
    lightGray = Color(0xfff8f8f8),
    gray = Color(0xFFE5E5E5),
    darkGray = Color(0xFF838383),
    text = Color(0xff212121),
    textSecondary = Color(0xFF656566),
    textDisabled = Color(0xFF9d9d9d),
    blue = Color(0xff4059bf),
    red = Color(0xffbf4040),
    green = Color(0xff7fbf40),
    orange = Color(0xffe8730e),

    primaryContainer = Color(0xff4046bf),
    onPrimaryContainer = Color.White,
    iconOnPrimaryContainer = Color.White,
//    primaryContainer = Color(0xffecedf9),
//    onPrimaryContainer = Color(0xff121212),
//    iconOnPrimaryContainer = Color(0xff2e30b8),
)

val LocalColorPalette = staticCompositionLocalOf { LightColorPalette }

@Composable
fun rememberColorPalette(isDarkTheme: Boolean = isSystemInDarkTheme()): ColorPalette {
    return remember(isDarkTheme) {
        if (isDarkTheme) DarkColorPalette else LightColorPalette
    }
}
