package it.vfsfitvnm.vimusic.utils

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.*
import kotlin.math.absoluteValue

open class PlayerState(val mediaController: Player) : Player.Listener {
    private val handler = Handler(Looper.getMainLooper())

    var currentPosition by mutableStateOf(mediaController.currentPosition)

    var duration by mutableStateOf(mediaController.duration)
        private set

    val progress: Float
        get() = currentPosition.toFloat() / duration.absoluteValue

    var playbackState by mutableStateOf(mediaController.playbackState)
        private set

    var mediaItemIndex by mutableStateOf(mediaController.currentMediaItemIndex)
        private set

    var mediaItem by mutableStateOf(mediaController.currentMediaItem)
        private set

    var mediaMetadata by mutableStateOf(mediaController.mediaMetadata)
        private set

    var isPlaying by mutableStateOf(mediaController.isPlaying)
        private set

    var playWhenReady by mutableStateOf(mediaController.playWhenReady)
        private set

    var repeatMode by mutableStateOf(mediaController.repeatMode)
        private set

    var error by mutableStateOf(mediaController.playerError)

    var mediaItems by mutableStateOf(mediaController.currentTimeline.mediaItems)
        private set

    var volume by mutableStateOf(mediaController.volume)
        private set

    init {
        handler.post(object : Runnable {
            override fun run() {
                currentPosition = mediaController.currentPosition
                handler.postDelayed(this, 500)
            }
        })
    }

    override fun onVolumeChanged(volume: Float) {
        this.volume = volume
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        duration = mediaController.duration
        this.playbackState = playbackState
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        this.mediaMetadata = mediaMetadata
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying = isPlaying
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        this.playWhenReady = playWhenReady
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        this.mediaItem = mediaItem
        mediaItemIndex = mediaController.currentMediaItemIndex
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        this.repeatMode = repeatMode
    }

    override fun onPlayerError(playbackException: PlaybackException) {
        error = playbackException
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        mediaItems = timeline.mediaItems
        mediaItemIndex = mediaController.currentMediaItemIndex
    }
}
