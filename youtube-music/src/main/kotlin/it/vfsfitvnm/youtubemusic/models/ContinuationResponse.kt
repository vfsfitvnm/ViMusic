package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable

@Serializable
data class ContinuationResponse(
    val continuationContents: ContinuationContents,
) {
    @Serializable
    data class ContinuationContents(
        val musicShelfContinuation: MusicShelfRenderer
    ) {
//        @Serializable
//        data class MusicShelfContinuation(
//            val continuations: List<Continuation>?,
//            val contents: List<MusicShelfRenderer.Content>
//        )
    }
}
