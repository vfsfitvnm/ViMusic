package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

context(DisposableEffectScope)
fun Player.listener(listener: Player.Listener): DisposableEffectResult {
    addListener(listener)
    return onDispose {
        removeListener(listener)
    }
}

@Composable
fun rememberMediaItemIndex(player: Player): State<Int> {
    val mediaItemIndexState = remember(player) {
        mutableStateOf(if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItemIndexState.value =
                    if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                mediaItemIndexState.value =
                    if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
            }
        })
    }

    return mediaItemIndexState
}

@Composable
fun rememberMediaItem(player: Player): State<MediaItem?> {
    val state = remember(player) {
        mutableStateOf(player.currentMediaItem)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                state.value = mediaItem
            }
        })
    }

    return state
}

@Composable
fun rememberWindows(player: Player): State<List<Timeline.Window>> {
    val windowsState = remember(player) {
        mutableStateOf(player.currentTimeline.windows)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                windowsState.value = timeline.windows
            }
        })
    }

    return windowsState
}

@Composable
fun rememberShouldBePlaying(player: Player): State<Boolean> {
    val state = remember(player) {
        mutableStateOf(!(player.playbackState == Player.STATE_ENDED || !player.playWhenReady))
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                state.value = !(player.playbackState == Player.STATE_ENDED || !playWhenReady)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                state.value = !(playbackState == Player.STATE_ENDED || !player.playWhenReady)
            }
        })
    }

    return state
}

@Composable
fun rememberRepeatMode(player: Player): State<Int> {
    val state = remember(player) {
        mutableStateOf(player.repeatMode)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onRepeatModeChanged(repeatMode: Int) {
                state.value = repeatMode
            }
        })
    }

    return state
}

@Composable
fun rememberPositionAndDuration(player: Player): State<Pair<Long, Long>> {
    val state = produceState(initialValue = player.currentPosition to player.duration) {
        var isSeeking = false

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isSeeking = false
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                value = player.currentPosition to value.second
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    isSeeking = true
                    value = player.currentPosition to player.duration
                }
            }
        }

        player.addListener(listener)

        val pollJob = launch {
            while (isActive) {
                delay(500)
                if (!isSeeking) {
                    value = player.currentPosition to player.duration
                }
            }
        }

        awaitDispose {
            pollJob.cancel()
            player.removeListener(listener)
        }
    }

    return state
}

@Composable
fun rememberVolume(player: Player): State<Float> {
    val volumeState = remember(player) {
        mutableStateOf(player.volume)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onVolumeChanged(volume: Float) {
                volumeState.value = volume
            }
        })
    }

    return volumeState
}

@Composable
fun rememberError(player: Player): State<PlaybackException?> {
    val errorState = remember(player) {
        mutableStateOf(player.playerError)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                errorState.value = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                errorState.value = playbackException
            }
        })
    }

    return errorState
}
