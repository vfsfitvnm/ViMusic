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
        Header(title = stringResource(id = R.string.about)) {
            BasicText(
                text = "v${BuildConfig.VERSION_NAME} " + stringResource(id = R.string.by_dev_name),
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = stringResource(id = R.string.social).uppercase())

        SettingsEntry(
            title = "GitHub",
            text = stringResource(id = R.string.view_source_code),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(id = R.string.troubleshooting_uppercase))

        SettingsEntry(
            title = stringResource(id = R.string.report_an_issue),
            text = stringResource(id = R.string.you_will_be_redirected_to_GitHub),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )

        SettingsEntry(
            title = stringResource(id = R.string.request_feature_or_suggest_an_idea),
            text = stringResource(id = R.string.you_will_be_redirected_to_GitHub),
            onClick = {
                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=enhancement&template=feature_request.yaml")
            }
        )
    }
}
