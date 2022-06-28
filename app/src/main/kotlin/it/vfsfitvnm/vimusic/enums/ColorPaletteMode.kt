package it.vfsfitvnm.vimusic.enums

import it.vfsfitvnm.vimusic.ui.styling.BlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.ColorPalette
import it.vfsfitvnm.vimusic.ui.styling.DarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LightColorPalette

enum class ColorPaletteMode {
    Light,
    Dark,
    Black,
    System;

    fun palette(isSystemInDarkMode: Boolean): ColorPalette {
        return when (this) {
            Light -> LightColorPalette
            Dark -> DarkColorPalette
            Black -> BlackColorPalette
            System -> when (isSystemInDarkMode) {
                true -> DarkColorPalette
                false -> LightColorPalette
            }
        }
    }
}
