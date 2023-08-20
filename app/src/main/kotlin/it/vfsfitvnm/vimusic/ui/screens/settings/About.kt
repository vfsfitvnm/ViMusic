package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.secondary

@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current

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
        Header(title = "About") {
            BasicText(
                text = "v${BuildConfig.VERSION_NAME} by vfsfitvnm",
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = "SOCIAL")

        SettingsEntry(
            title = "GitHub",
            text = stringResource(R.string.view_the_source_code),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "TROUBLESHOOTING")

        SettingsEntry(
            title = stringResource(R.string.report_an_issue),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )

        SettingsEntry(
            title = stringResource(R.string.request_a_feature_or_suggest_an_idea),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=enhancement&template=feature_request.yaml")
            }
        )
    }
}
