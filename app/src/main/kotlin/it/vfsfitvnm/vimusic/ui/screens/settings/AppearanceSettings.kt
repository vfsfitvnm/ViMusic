package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.applyFontPaddingKey
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid13
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey
import it.vfsfitvnm.vimusic.utils.useSystemFontKey

@ExperimentalAnimationApi
@Composable
fun AppearanceSettings() {
    val (colorPalette) = LocalAppearance.current

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Light
    )
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        false
    )

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = stringResource(id = R.string.appearance))

        SettingsEntryGroupText(title = stringResource(id = R.string.colors).uppercase())

        val default = stringResource(id = R.string.default_word)
        val dynamic = stringResource(id = R.string.dynamic)
        val pureBlack = stringResource(id = R.string.pureblack)

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.theme),
            selectedValue = colorPaletteName,
            valueText = {
                when (it) {
                    ColorPaletteName.Default -> default
                    ColorPaletteName.PureBlack -> pureBlack
                    ColorPaletteName.Dynamic -> dynamic
                }
            },
            onValueSelected = { colorPaletteName = it }
        )

        val lightTheme = stringResource(id = R.string.light_theme)
        val darkTheme = stringResource(id = R.string.dark_theme)
        val systemTheme = stringResource(id = R.string.system)

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.theme_mode),
            selectedValue = colorPaletteMode,
            valueText = {
                when (it) {
                    ColorPaletteMode.Light -> lightTheme
                    ColorPaletteMode.Dark -> darkTheme
                    ColorPaletteMode.System -> systemTheme
                }
            },
            isEnabled = colorPaletteName != ColorPaletteName.PureBlack,
            onValueSelected = { colorPaletteMode = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(id = R.string.shapes).uppercase())

        val noneShape = stringResource(id = R.string.none_shape)
        val lightShape = stringResource(id = R.string.light_shape)
        val mediumShape = stringResource(id = R.string.medium)
        val heavyShape = stringResource(id = R.string.heavy_shape)

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.thumbnail_roundness),
            selectedValue = thumbnailRoundness,
            onValueSelected = { thumbnailRoundness = it },
            valueText = {
                when (it) {
                    ThumbnailRoundness.None -> noneShape
                    ThumbnailRoundness.Light -> lightShape
                    ThumbnailRoundness.Medium -> mediumShape
                    ThumbnailRoundness.Heavy -> heavyShape
                }
            },
            trailingContent = {
                Spacer(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorPalette.accent,
                            shape = thumbnailRoundness.shape()
                        )
                        .background(
                            color = colorPalette.background1,
                            shape = thumbnailRoundness.shape()
                        )
                        .size(36.dp)
                )
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(id = R.string.text).uppercase())

        SwitchSettingEntry(
            title = stringResource(id = R.string.use_system_font),
            text = stringResource(id = R.string.use_system_font_description),
            isChecked = useSystemFont,
            onCheckedChange = { useSystemFont = it }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.apply_font_padding),
            text = stringResource(id = R.string.apply_font_padding_description),
            isChecked = applyFontPadding,
            onCheckedChange = { applyFontPadding = it }
        )

        if (!isAtLeastAndroid13) {
            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(id = R.string.lockscreen).uppercase())

            SwitchSettingEntry(
                title = stringResource(id = R.string.show_song_cover),
                text = stringResource(id = R.string.show_song_cover_description),
                isChecked = isShowingThumbnailInLockscreen,
                onCheckedChange = { isShowingThumbnailInLockscreen = it }
            )
        }
    }
}
