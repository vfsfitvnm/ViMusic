package it.vfsfitvnm.vimusic.utils

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.media3.common.*
import kotlin.math.absoluteValue

@Stable
class PlayerState(private val player: Player) : Player.Listener, Runnable {
    private val handler = Handler(Looper.getMainLooper())

    var currentPosition by mutableStateOf(player.currentPosition)

    var duration by mutableStateOf(player.duration)
        private set

    val progress: Float
        get() = currentPosition.toFloat() / duration.absoluteValue

    var playbackState by mutableStateOf(player.playbackState)
        private set

    var mediaItemIndex by mutableStateOf(player.currentMediaItemIndex)
        private set

    var mediaItem by mutableStateOf(player.currentMediaItem)
        private set

    var mediaMetadata by mutableStateOf(player.mediaMetadata)
        private set

    var playWhenReady by mutableStateOf(player.playWhenReady)
        private set

    var repeatMode by mutableStateOf(player.repeatMode)
        private set

    var error by mutableStateOf(player.playerError)

    var mediaItems by mutableStateOf(player.currentTimeline.mediaItems)
        private set

    var volume by mutableStateOf(player.volume)
        private set

    override fun onVolumeChanged(volume: Float) {
        this.volume = volume
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        this.playbackState = playbackState
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        this.mediaMetadata = mediaMetadata
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        this.playWhenReady = playWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        this.mediaItem = mediaItem
        mediaItemIndex = player.currentMediaItemIndex
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        this.repeatMode = repeatMode
    }

    override fun onPlayerError(playbackException: PlaybackException) {
        error = playbackException
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        mediaItems = timeline.mediaItems
        mediaItemIndex = player.currentMediaItemIndex
    }

    override fun run() {
        duration = player.duration
        currentPosition = player.currentPosition
        handler.postDelayed(this, 500)
    }

    fun init() {
        player.addListener(this)
        handler.post(this)
    }

    fun dispose() {
        player.removeListener(this)
        handler.removeCallbacks(this)
    }
}

@Composable
fun rememberPlayerState(
    player: Player?
): PlayerState? {
    val playerState = remember(player) {
        player?.let(::PlayerState)
    }

    playerState?.let {
        DisposableEffect(Unit) {
            playerState.init()
            onDispose(playerState::dispose)
        }
    }

    return playerState
}
