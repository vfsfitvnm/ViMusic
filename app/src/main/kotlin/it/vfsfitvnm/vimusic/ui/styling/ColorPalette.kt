package it.vfsfitvnm.vimusic.ui.styling

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorPalette(
    val background: Color,
    val elevatedBackground: Color,
    val lightBackground: Color,
    val backgroundContainer: Color,
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
    val magenta: Color,
    val cyan: Color,

    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val iconOnPrimaryContainer: Color,

    val isDark: Boolean
)

val DarkColorPalette = ColorPalette(
    background = Color(0xff16171d),
    lightBackground = Color(0xff1f2029),
    elevatedBackground = Color(0xff1f2029),
    backgroundContainer = Color(0xff2b2d3b),
    text = Color(0xffe1e1e2),
    textSecondary = Color(0xffa3a4a6),
    textDisabled = Color(0xff6f6f73),
    lightGray = Color(0xfff8f8f8),
    gray = Color(0xFFE5E5E5),
    darkGray = Color(0xFF838383),
    blue = Color(0xff507fdd),
    red = Color(0xffbf4040),
    green = Color(0xff82b154),
    orange = Color(0xffe9a033),
    magenta = Color(0xffbb4da4),
    cyan = Color(0xFF4DA5BB),
    primaryContainer = Color(0xff4046bf),
    onPrimaryContainer = Color.White,
    iconOnPrimaryContainer = Color.White,
    isDark = true
)

val BlackColorPalette = DarkColorPalette.copy(
    background = Color.Black,
    lightBackground = Color(0xff0d0d12),
    elevatedBackground = Color(0xff0d0d12),
    backgroundContainer = Color(0xff0d0d12)
)

val LightColorPalette = ColorPalette(
    background = Color(0xfffdfdfe),
    lightBackground = Color(0xFFf8f8fc),
    elevatedBackground = Color(0xfffdfdfe),
    backgroundContainer = Color(0xffeaeaf5),
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
    magenta = Color(0xffbb4da4),
    cyan = Color(0xFF4DBBB2),
    primaryContainer = Color(0xff4046bf),
    onPrimaryContainer = Color.White,
    iconOnPrimaryContainer = Color.White,
    isDark = false
)
