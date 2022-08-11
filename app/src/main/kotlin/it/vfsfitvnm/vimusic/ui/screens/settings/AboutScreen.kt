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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.size.Dimension
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.SettingsDescription
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import kotlinx.coroutines.FlowPreview

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

                SettingsTitle(text = stringResource(R.string.about))

                SettingsDescription(text = "v${BuildConfig.VERSION_NAME}\nby vfsfitvnm, forked by Malopieds")

                SettingsEntryGroupText(title = stringResource(R.string.social))

                SettingsEntry(
                    title = "GitHub",
                    text = stringResource(R.string.source_code),
                    onClick = {
                        uriHandler.openUri("https://github.com/Malopieds/ViMusic")
                    }
                )

                SettingsEntryGroupText(title = stringResource(R.string.troubleshooting))

                SettingsEntry(
                    title = stringResource(R.string.report_issue),
                    text = stringResource(R.string.redir_gh),
                    onClick = {
                        uriHandler.openUri("https://github.com/Malopieds/ViMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
                    }
                )

                SettingsEntry(
                    title = stringResource(R.string.feature_request),
                    text = stringResource(R.string.redir_gh),
                    onClick = {
                        uriHandler.openUri("https://github.com/Malopieds/ViMusic/issues/new?assignees=&labels=enhancement&template=feature_request.yaml")
                    }
                )
            }
        }
    }
}
