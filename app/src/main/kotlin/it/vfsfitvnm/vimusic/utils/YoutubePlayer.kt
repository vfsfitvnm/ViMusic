package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.*
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex

class YoutubePlayer(mediaController: MediaController) : PlayerState(mediaController) {
    object Radio {
        var isActive by mutableStateOf(false)

        var listener: Listener? = null

        private var videoId: String? = null
        private var playlistId: String? = null
        private var playlistSetVideoId: String? = null
        private var parameters: String? = null

        var nextContinuation by mutableStateOf<Outcome<String?>>(Outcome.Initial)

        fun setup(videoId: String? = null, playlistId: String? = null, playlistSetVideoId: String? = null, parameters: String? = null) {
            this.videoId = videoId
            this.playlistId = playlistId
            this.playlistSetVideoId = playlistSetVideoId
            this.parameters = parameters

            isActive = true
            nextContinuation = Outcome.Initial
        }

        fun setup(watchEndpoint: NavigationEndpoint.Endpoint.Watch?) {
            setup(
                videoId = watchEndpoint?.videoId,
                playlistId = watchEndpoint?.playlistId,
                parameters = watchEndpoint?.params,
                playlistSetVideoId = watchEndpoint?.playlistSetVideoId
            )

            listener?.process(true)
        }

        suspend fun process(player: Player, force: Boolean = false, play: Boolean = false) {
            if (!isActive) return

            if (!force && !play) {
                val isFirstSong = withContext(Dispatchers.Main) {
                    player.mediaItemCount == 0 || (player.currentMediaItemIndex == 0 && player.mediaItemCount == 1)
                }
                val isNearEndSong = withContext(Dispatchers.Main) {
                    player.mediaItemCount - player.currentMediaItemIndex <= 3
                }

                if (!isFirstSong && !isNearEndSong) {
                    return
                }
            }

            val token = nextContinuation.valueOrNull

            nextContinuation = Outcome.Loading

            nextContinuation = withContext(Dispatchers.IO) {
                YouTube.next(
                    videoId = videoId ?: withContext(Dispatchers.Main) {
                        player.lastMediaItem?.mediaId ?: error("This should not happen")
                    },
                    playlistId = playlistId,
                    params = parameters,
                    playlistSetVideoId = playlistSetVideoId,
                    continuation = token
                )
            }.map { nextResult ->
                nextResult.items?.map(it.vfsfitvnm.youtubemusic.YouTube.Item.Song::asMediaItem)?.let { mediaItems ->
                    withContext(Dispatchers.Main) {
                        if (play) {
                            player.forcePlayFromBeginning(mediaItems)
                        } else {
                            player.addMediaItems(mediaItems.drop(if (token == null) 1 else 0))
                        }
                    }
                }

                nextResult.continuation?.takeUnless { token == nextResult.continuation }
            }.recoverWith(token)
        }

        fun reset() {
            videoId = null
            playlistId = null
            playlistSetVideoId = null
            parameters = null
            isActive = false
            nextContinuation = Outcome.Initial
        }

        interface Listener {
            fun process(play: Boolean)
        }
    }
}

val LocalYoutubePlayer = compositionLocalOf<YoutubePlayer?> { null }

@Composable
fun rememberYoutubePlayer(
    mediaControllerFuture: ListenableFuture<MediaController>,
    block: (MediaController) -> Unit,
): YoutubePlayer? {
    val mediaController by produceState<MediaController?>(initialValue = null) {
        value = mediaControllerFuture.await().also(block)
    }

    val playerState = remember(mediaController) {
        YoutubePlayer(mediaController ?: return@remember null).also {
            // TODO: should we remove the listener later on?
            mediaController?.addListener(it)
        }
    }

    return playerState
}
