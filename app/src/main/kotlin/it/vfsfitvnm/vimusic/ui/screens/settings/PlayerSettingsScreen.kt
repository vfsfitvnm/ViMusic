package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.SwitchSettingEntry
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.persistentQueueKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.skipSilenceKey
import it.vfsfitvnm.vimusic.utils.volumeNormalizationKey

@ExperimentalAnimationApi
@Composable
fun PlayerSettingsScreen() {

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val context = LocalContext.current
            val (colorPalette) = LocalAppearance.current
            val binder = LocalPlayerServiceBinder.current

            var persistentQueue by rememberPreference(persistentQueueKey, false)
            var skipSilence by rememberPreference(skipSilenceKey, false)
            var volumeNormalization by rememberPreference(volumeNormalizationKey, false)

            val activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                }

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

                SettingsTitle(text = stringResource(R.string.player_audio))

                SettingsEntryGroupText(title = stringResource(R.string.player))

                SwitchSettingEntry(
                    title = stringResource(R.string.pers_queue),
                    text = stringResource(R.string.pers_queue_desc),
                    isChecked = persistentQueue,
                    onCheckedChange = {
                        persistentQueue = it
                    }
                )

                SettingsEntryGroupText(title = stringResource(R.string.audio))

                SwitchSettingEntry(
                    title = stringResource(R.string.skip_silence),
                    text = stringResource(R.string.skip_silence_desc),
                    isChecked = skipSilence,
                    onCheckedChange = {
                        skipSilence = it
                    }
                )

                SwitchSettingEntry(
                    title = stringResource(R.string.loud_norm),
                    text = stringResource(R.string.loud_norm_desc),
                    isChecked = volumeNormalization,
                    onCheckedChange = {
                        volumeNormalization = it
                    }
                )

                SettingsEntry(
                    title = stringResource(R.string.equalizer),
                    text = stringResource(R.string.equalizer_desc),
                    onClick = {
                        val intent =
                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                putExtra(
                                    AudioEffect.EXTRA_AUDIO_SESSION,
                                    binder?.player?.audioSessionId
                                )
                                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                putExtra(
                                    AudioEffect.EXTRA_CONTENT_TYPE,
                                    AudioEffect.CONTENT_TYPE_MUSIC
                                )
                            }

                        if (intent.resolveActivity(context.packageManager) != null) {
                            activityResultLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, "No equalizer app found!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }
    }
}
