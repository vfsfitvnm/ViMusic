package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Continuation(
    @JsonNames("nextContinuationData", "nextRadioContinuationData")
    val nextRadioContinuationData: Data
) {
    @Serializable
    data class Data(
        val continuation: String
    )
}
