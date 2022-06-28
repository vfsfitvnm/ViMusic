package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class YouTubeRadio(
    private val videoId: String? = null,
    private val playlistId: String? = null,
    private val playlistSetVideoId: String? = null,
    private val parameters: String? = null
) {
    var nextContinuation by mutableStateOf<Outcome<String?>>(Outcome.Initial)

    suspend fun process(): List<MediaItem> {
        val token = nextContinuation.valueOrNull

        nextContinuation = Outcome.Loading

        var mediaItems: List<MediaItem>? = null

        nextContinuation = withContext(Dispatchers.IO) {
            YouTube.next(
                videoId = videoId ?: error("This should not happen"),
                playlistId = playlistId,
                params = parameters,
                playlistSetVideoId = playlistSetVideoId,
                continuation = token
            )
        }.map { nextResult ->
            mediaItems = nextResult.items?.map(YouTube.Item.Song::asMediaItem)

            nextResult.continuation?.takeUnless { token == nextResult.continuation }
        }.recoverWith(token)

        return mediaItems ?: emptyList()
    }
}
