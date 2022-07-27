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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.DisabledSettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@ExperimentalAnimationApi
@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette) = LocalAppearance.current
            val uriHandler = LocalUriHandler.current

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
                }

                SettingsTitle(text = "About")

                SettingsEntryGroupText(title = "INFO")

                DisabledSettingsEntry(
                    title = "ViMusic",
                    text = "v${BuildConfig.VERSION_NAME}",
                )

                SettingsEntry(
                    title = "Report an issue",
                    text = "You will be redirected to GitHub",
                    onClick = {
                        uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
                    }
                )

                SettingsEntry(
                    title = "Request a feature or suggest an idea",
                    text = "You will be redirected to GitHub",
                    onClick = {
                        uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=enhancement&template=feature_request.yaml")
                    }
                )

                SettingsEntryGroupText(title = "SOCIAL")

                SettingsEntry(
                    title = "GitHub",
                    text = "View the source code",
                    onClick = {
                        uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic")
                    }
                )
            }
        }
    }
}
