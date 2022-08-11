package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ColorPaletteName
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.EnumValueSelectorSettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.SwitchSettingEntry
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.colorPaletteModeKey
import it.vfsfitvnm.vimusic.utils.colorPaletteNameKey
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnailRoundnessKey

@ExperimentalAnimationApi
@Composable
fun AppearanceSettingsScreen() {
    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette) = LocalAppearance.current

            var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
            var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
            var thumbnailRoundness by rememberPreference(
                thumbnailRoundnessKey,
                ThumbnailRoundness.Light
            )
            var isShowingThumbnailInLockscreen by rememberPreference(
                isShowingThumbnailInLockscreenKey,
                true
            )

            Column(
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = Dimensions.collapsedPlayer)
                    .systemBarsPadding()
            ) {
                TopAppBar(
                    modifier = Modifier
                        .height(52.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.chevron_back),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = pop)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                SettingsTitle(text = stringResource(R.string.appearance))

                SettingsEntryGroupText(title = stringResource(R.string.colors))

                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.theme_mode),
                    selectedValue = colorPaletteName,
                    onValueSelected = {
                        colorPaletteName = it
                    }
                )

                EnumValueSelectorSettingsEntry(
                    title = "Theme mode",
                    selectedValue = colorPaletteMode,
                    isEnabled = colorPaletteName != ColorPaletteName.PureBlack,
                    onValueSelected = {
                        colorPaletteMode = it
                    }
                )

                SettingsEntryGroupText(title = stringResource(R.string.shapes))

                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.thumbnail_roundness),
                    selectedValue = thumbnailRoundness,
                    onValueSelected = {
                        thumbnailRoundness = it
                    }
                )

                SettingsEntryGroupText(title = "LOCKSCREEN")

                SwitchSettingEntry(
                    title = "Show song cover",
                    text = "Use the playing song cover as the lockscreen wallpaper",
                    isChecked = isShowingThumbnailInLockscreen,
                    onCheckedChange = { isShowingThumbnailInLockscreen = it }
                )
            }
        }
    }
}
