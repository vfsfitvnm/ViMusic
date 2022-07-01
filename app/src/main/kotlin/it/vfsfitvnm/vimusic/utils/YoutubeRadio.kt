package it.vfsfitvnm.vimusic.utils

import androidx.media3.common.MediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class YouTubeRadio(
    private val videoId: String? = null,
    private val playlistId: String? = null,
    private val playlistSetVideoId: String? = null,
    private val parameters: String? = null
) {
    private var nextContinuation: String? = null

    suspend fun process(): List<MediaItem> {
        var mediaItems: List<MediaItem>? = null

        nextContinuation = withContext(Dispatchers.IO) {
            YouTube.next(
                videoId = videoId,
                playlistId = playlistId,
                params = parameters,
                playlistSetVideoId = playlistSetVideoId,
                continuation = nextContinuation
            )?.getOrNull()?.let { nextResult ->
                mediaItems = nextResult.items?.map(YouTube.Item.Song::asMediaItem)
                nextResult.continuation?.takeUnless { nextContinuation == nextResult.continuation }
            }
        }

        return mediaItems ?: emptyList()
    }
}
