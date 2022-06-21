package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.media3.common.C
import androidx.media3.session.SessionResult
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.services.*
import it.vfsfitvnm.vimusic.ui.components.ChunkyButton
import it.vfsfitvnm.vimusic.ui.components.Pager
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.DefaultDialog
import it.vfsfitvnm.vimusic.ui.screens.*
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive


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

            val audioSessionId by produceState(initialValue = C.AUDIO_SESSION_ID_UNSET, mediaController) {
                val hasEqualizer = context.packageManager.resolveActivity(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL),
                    0
                ) != null

                if (hasEqualizer) {
                    value =
                        mediaController?.sendCustomCommand(GetAudioSessionIdCommand, Bundle.EMPTY)
                            ?.await()?.extras?.getInt("audioSessionId", C.AUDIO_SESSION_ID_UNSET)
                            ?: C.AUDIO_SESSION_ID_UNSET
                }
            }

            var sleepTimerMillisLeft by remember {
                mutableStateOf<Long?>(null)
            }

            LaunchedEffect(mediaController) {
                while (isActive) {
                    sleepTimerMillisLeft =
                        mediaController?.syncCommand(GetSleepTimerMillisLeftCommand)
                            ?.takeIf { it.resultCode == SessionResult.RESULT_SUCCESS }
                            ?.extras?.getLong("millisLeft")
                    delay(1000)
                }
            }

            var isShowingSleepTimerDialog by remember {
                mutableStateOf(false)
            }

            if (isShowingSleepTimerDialog) {
                if (sleepTimerMillisLeft != null) {
                    ConfirmationDialog(
                        text = "Do you want to stop the sleep timer?",
                        cancelText = "No",
                        confirmText = "Stop",
                        onDismiss = {
                            isShowingSleepTimerDialog = false
                        },
                        onConfirm = {
                            mediaController?.syncCommand(CancelSleepTimerCommand)
                            sleepTimerMillisLeft = null
                        }
                    )
                } else {
                    DefaultDialog(
                        onDismiss = {
                            isShowingSleepTimerDialog = false
                        },
                        modifier = Modifier
                    ) {
                        var hours by remember {
                            mutableStateOf(0)
                        }

                        var minutes by remember {
                            mutableStateOf(0)
                        }

                        BasicText(
                            text = "Set sleep timer",
                            style = typography.s.semiBold,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 24.dp)
                        )

                        Row(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        ) {
                            Pager(
                                selectedIndex = hours,
                                onSelectedIndex = {
                                    hours = it
                                },
                                orientation = Orientation.Vertical,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(72.dp)
                            ) {
                                repeat(12) {
                                    BasicText(
                                        text = "$it h",
                                        style = typography.xs.semiBold
                                    )
                                }
                            }

                            Pager(
                                selectedIndex = minutes,
                                onSelectedIndex = {
                                    minutes = it
                                },
                                orientation = Orientation.Vertical,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(72.dp)
                            ) {
                                repeat(4) {
                                    BasicText(
                                        text = "${it * 15} m",
                                        style = typography.xs.semiBold
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            ChunkyButton(
                                backgroundColor = colorPalette.lightBackground,
                                text = "Cancel",
                                textStyle = typography.xs.semiBold,
                                shape = RoundedCornerShape(36.dp),
                                onClick = { isShowingSleepTimerDialog = false }
                            )

                            ChunkyButton(
                                backgroundColor = colorPalette.primaryContainer,
                                text = "Set",
                                textStyle = typography.xs.semiBold.color(colorPalette.onPrimaryContainer),
                                shape = RoundedCornerShape(36.dp),
                                isEnabled = hours > 0 || minutes > 0,
                                onClick = {
                                    mediaController?.syncCommand(
                                        SetSleepTimerCommand,
                                        bundleOf("delayMillis" to (hours * 60 + minutes * 15) * 60 * 1000L)
                                    )
                                    sleepTimerMillisLeft =
                                        mediaController?.syncCommand(GetSleepTimerMillisLeftCommand)?.extras?.getLong(
                                            "millisLeft"
                                        )
                                    isShowingSleepTimerDialog = false
                                }
                            )
                        }
                    }
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

                SwitchSettingEntry(
                    title = "Loudness normalization",
                    text = "Lower the volume to a standard level",
                    isChecked = preferences.volumeNormalization,
                    onCheckedChange = {
                        preferences.volumeNormalization = it
                    }
                )

                SettingsEntry(
                    title = "Equalizer",
                    text = "Interact with the system equalizer",
                    onClick = {
                        activityResultLauncher.launch(
                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                                putExtra(
                                    AudioEffect.EXTRA_CONTENT_TYPE,
                                    AudioEffect.CONTENT_TYPE_MUSIC
                                )
                            }
                        )
                    },
                    isEnabled = audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != AudioEffect.ERROR_BAD_VALUE
                )

                SettingsEntry(
                    title = "Sleep timer",
                    text = sleepTimerMillisLeft?.let { "${DateUtils.formatElapsedTime(it / 1000)} left" }
                        ?: "Stop the music after a period of time",
                    onClick = {
                        isShowingSleepTimerDialog = true
                    }
                )
            }
        }
    }
}