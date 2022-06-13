package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.media3.common.C
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.services.GetAudioSessionIdCommand
import it.vfsfitvnm.vimusic.services.SetSkipSilenceCommand
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.*
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.LocalPreferences
import it.vfsfitvnm.vimusic.utils.LocalYoutubePlayer
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlinx.coroutines.guava.await


@ExperimentalAnimationApi
@Composable
fun PlayerSettingsScreen() {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val context = LocalContext.current
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val preferences = LocalPreferences.current
            val mediaController = LocalYoutubePlayer.current?.mediaController

            val activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                }

            val audioSessionId by produceState(initialValue = C.AUDIO_SESSION_ID_UNSET) {
                val hasEqualizer = context.packageManager.resolveActivity(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL),
                    0
                ) != null

                println("hasEqualizer? $hasEqualizer")

                if (hasEqualizer) {
                    value =
                        mediaController?.sendCustomCommand(GetAudioSessionIdCommand, Bundle.EMPTY)
                            ?.await()?.extras?.getInt("audioSessionId", C.AUDIO_SESSION_ID_UNSET)
                            ?: C.AUDIO_SESSION_ID_UNSET
                }
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

                    BasicText(
                        text = "Player & Audio",
                        style = typography.m.semiBold
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

//                SwitchSettingEntry(
//                    title = "Persistent queue",
//                    text = "Save and restore playing songs",
//                    isChecked = preferences.persistentQueue,
//                    onCheckedChange = {
//                        preferences.persistentQueue = it
//                    },
//                    isEnabled = false
//                )

                SwitchSettingEntry(
                    title = "Skip silence",
                    text = "Skip silent parts during playback",
                    isChecked = preferences.skipSilence,
                    onCheckedChange = {
                        mediaController?.sendCustomCommand(
                            SetSkipSilenceCommand,
                            bundleOf("skipSilence" to it)
                        )
                        preferences.skipSilence = it
                    }
                )

                SettingsEntry(
                    title = "Equalizer",
                    text = "Interact with the system equalizer",
                    onClick = {
                        activityResultLauncher.launch(
                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                            }
                        )
                    },
                    isEnabled = audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != AudioEffect.ERROR_BAD_VALUE
                )
            }
        }
    }
}