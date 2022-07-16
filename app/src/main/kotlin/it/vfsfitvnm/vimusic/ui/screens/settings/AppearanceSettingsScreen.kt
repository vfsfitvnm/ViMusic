package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.*
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.*

@ExperimentalAnimationApi
@Composable
fun AppearanceSettingsScreen() {
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            AlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val (colorPalette, typography) = LocalAppearance.current

            var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
            var thumbnailRoundness by rememberPreference(thumbnailRoundnessKey, ThumbnailRoundness.Light)
            var isCachedPlaylistShown by rememberPreference(isCachedPlaylistShownKey, false)

            Column(
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp)
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

                    BasicText(
                        text = "Appearance",
                        style = typography.m.semiBold
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                SettingsEntryGroupText(title = "COLORS")

                EnumValueSelectorSettingsEntry(
                    title = "Theme mode",
                    selectedValue = colorPaletteMode,
                    onValueSelected = {
                        colorPaletteMode = it
                    }
                )

                SettingsEntryGroupText(title = "SHAPES")

                EnumValueSelectorSettingsEntry(
                    title = "Thumbnail roundness",
                    selectedValue = thumbnailRoundness,
                    onValueSelected = {
                        thumbnailRoundness = it
                    }
                )

                SettingsEntryGroupText(title = "OTHER")

                SwitchSettingEntry(
                    title = "Cached playlist",
                    text = "Display a playlist whose songs can be played offline",
                    isChecked = isCachedPlaylistShown,
                    onCheckedChange = {
                        isCachedPlaylistShown = it
                    }
                )
            }
        }
    }
}
