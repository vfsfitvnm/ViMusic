package it.vfsfitvnm.vimusic.ui.styling

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName

@Immutable
data class ColorPalette(
    val background0: Color,
    val background1: Color,
    val background2: Color,
    val accent: Color,
    val onAccent: Color,
    val red: Color = Color(0xffbf4040),
    val blue: Color = Color(0xff4472cf),
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val isDark: Boolean
)

val DefaultDarkColorPalette = ColorPalette(
    background0 = Color(0xff16171d),
    background1 = Color(0xff1f2029),
    background2 = Color(0xff2b2d3b),
    text = Color(0xffe1e1e2),
    textSecondary = Color(0xffa3a4a6),
    textDisabled = Color(0xff6f6f73),
    accent = Color(0xff4046bf),
    onAccent = Color.White,
    isDark = true
)

val DefaultLightColorPalette = ColorPalette(
    background0 = Color(0xfffdfdfe),
    background1 = Color(0xfff8f8fc),
    background2 = Color(0xffeaeaf5),
    text = Color(0xff212121),
    textSecondary = Color(0xff656566),
    textDisabled = Color(0xff9d9d9d),
    accent = Color(0xff4046bf),
    onAccent = Color.White,
    isDark = false
)

val PureBlackColorPalette = DefaultDarkColorPalette.copy(
    background0 = Color.Black,
    background1 = Color.Black,
    background2 = Color.Black
)

fun colorPaletteOf(
    colorPaletteName: ColorPaletteName,
    colorPaletteMode: ColorPaletteMode,
    isSystemInDarkMode: Boolean
): ColorPalette {
    return when (colorPaletteName) {
        ColorPaletteName.Default, ColorPaletteName.Dynamic -> when (colorPaletteMode) {
            ColorPaletteMode.Light -> DefaultLightColorPalette
            ColorPaletteMode.Dark -> DefaultDarkColorPalette
            ColorPaletteMode.System -> when (isSystemInDarkMode) {
                true -> DefaultDarkColorPalette
                false -> DefaultLightColorPalette
            }
        }
        ColorPaletteName.PureBlack -> PureBlackColorPalette
    }
}

fun dynamicColorPaletteOf(bitmap: Bitmap, isDark: Boolean): ColorPalette? {
    val palette = Palette
        .from(bitmap)
        .maximumColorCount(8)
        .addFilter(if (isDark) ({ _, hsl -> hsl[0] !in 36f..100f }) else null)
        .generate()

    val hsl = if (isDark) {
        palette.dominantSwatch ?: Palette
            .from(bitmap)
            .maximumColorCount(8)
            .generate()
            .dominantSwatch
    } else {
        palette.dominantSwatch
    }?.hsl ?: return null

    return if (hsl[1] < 0.08) {
        val newHsl = palette.swatches
            .map(Palette.Swatch::getHsl)
            .sortedByDescending(FloatArray::component2)
            .find { it[1] != 0f }
            ?: hsl

        dynamicColorPaletteOf(newHsl, isDark)
    } else {
        dynamicColorPaletteOf(hsl, isDark)
    }
}

private fun dynamicColorPaletteOf(hsl: FloatArray, isDark: Boolean): ColorPalette {
    return colorPaletteOf(ColorPaletteName.Dynamic, if (isDark) ColorPaletteMode.Dark else ColorPaletteMode.Light, false).copy(
        background0 = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.1f), if (isDark) 0.10f else 0.925f),
        background1 = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.3f), if (isDark) 0.15f else 0.90f),
        background2 = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.4f), if (isDark) 0.2f else 0.85f),
        accent = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.5f), 0.5f),
        text = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.02f), if (isDark) 0.88f else 0.12f),
        textSecondary = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.1f), if (isDark) 0.65f else 0.40f),
        textDisabled = Color.hsl(hsl[0], hsl[1].coerceAtMost(0.2f), if (isDark) 0.40f else 0.65f),
    )
}

inline val ColorPalette.collapsedPlayerProgressBar: Color
    get() = if (this === DefaultDarkColorPalette || this === DefaultLightColorPalette || this == PureBlackColorPalette) {
        text
    } else {
        accent
    }

inline val ColorPalette.favoritesIcon: Color
    get() = if (this === DefaultDarkColorPalette || this === DefaultLightColorPalette || this == PureBlackColorPalette) {
        red
    } else {
        accent
    }

inline val ColorPalette.shimmer: Color
    get() = if (this === DefaultDarkColorPalette || this === DefaultLightColorPalette || this == PureBlackColorPalette) {
        Color(0xff838383)
    } else {
        accent
    }

inline val ColorPalette.overlay: Color
    get() = PureBlackColorPalette.background0.copy(alpha = 0.75f)

inline val ColorPalette.onOverlay: Color
    get() = PureBlackColorPalette.text

inline val ColorPalette.onOverlayShimmer: Color
    get() = PureBlackColorPalette.shimmer
