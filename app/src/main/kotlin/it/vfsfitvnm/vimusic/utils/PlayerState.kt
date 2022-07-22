package it.vfsfitvnm.vimusic.utils

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.media3.common.*
import kotlin.math.absoluteValue


@Stable
data class PlayerState(
    val currentPosition: Long,
    val duration: Long,
    val playbackState: Int,
    val mediaItemIndex: Int,
    val mediaItem: MediaItem?,
    val mediaMetadata: MediaMetadata,
    val playWhenReady: Boolean,
    val repeatMode: Int,
    val mediaItems: List<MediaItem>,
) {
    constructor(player: Player) : this(
        currentPosition = player.currentPosition,
        duration = player.duration,
        playbackState = player.playbackState,
        mediaItemIndex = player.currentMediaItemIndex,
        mediaItem = player.currentMediaItem,
        mediaMetadata = player.mediaMetadata,
        playWhenReady = player.playWhenReady,
        repeatMode = player.repeatMode,
        mediaItems = player.currentTimeline.mediaItems
    )

    val progress: Float
        get() = currentPosition.toFloat() / duration.absoluteValue
}


@Composable
fun rememberPlayerState(
    player: Player?
): PlayerState? {
    var playerState by remember(player) {
        mutableStateOf(player?.let(::PlayerState))
    }

    DisposableEffect(player) {
        if (player == null) return@DisposableEffect onDispose { }

        var isSeeking = false

        val handler = Handler(Looper.getMainLooper())

        val listener = object : Player.Listener, Runnable {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playerState = playerState?.copy(playbackState = playbackState)

                if (playbackState == Player.STATE_READY) {
                    isSeeking = false
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                playerState = playerState?.copy(mediaMetadata = mediaMetadata)
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                playerState = playerState?.copy(playWhenReady = playWhenReady)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                playerState = playerState?.copy(
                    currentPosition = player.currentPosition,
                    mediaItem = mediaItem,
                    mediaItemIndex = player.currentMediaItemIndex
                )
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                playerState = playerState?.copy(repeatMode = repeatMode)
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                playerState = playerState?.copy(
                    mediaItems = timeline.mediaItems,
                    mediaItemIndex = player.currentMediaItemIndex
                )
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    isSeeking = true
                    playerState = playerState?.copy(
                        duration = player.duration,
                        currentPosition = player.currentPosition
                    )
                }
            }

            override fun run() {
                if (!isSeeking) {
                    playerState = playerState?.copy(
                        duration = player.duration,
                        currentPosition = player.currentPosition
                    )
                }

                handler.postDelayed(this, 500)
            }
        }

        player.addListener(listener)
        handler.post(listener)

        onDispose {
            player.removeListener(listener)
            handler.removeCallbacks(listener)
        }
    }

    return playerState
}

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
        mutableStateOf(player.currentMediaItemIndex)
    }

    DisposableEffect(player) {
        player.listener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItemIndexState.value = player.currentMediaItemIndex
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                mediaItemIndexState.value = player.currentMediaItemIndex
            }
        })
    }

    return mediaItemIndexState
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