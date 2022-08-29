package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ContinuationResponse(
    val continuationContents: ContinuationContents,
) {
    @Serializable
    data class ContinuationContents(
        @JsonNames("musicPlaylistShelfContinuation")
        val musicShelfContinuation: MusicShelfRenderer?
    ) {
//        @Serializable
//        data class MusicShelfContinuation(
//            val continuations: List<Continuation>?,
//            val contents: List<MusicShelfRenderer.Content>
//        )
    }
}
