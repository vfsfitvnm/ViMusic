package it.vfsfitvnm.kugou.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class SearchLyricsResponse(
    val candidates: List<Candidate>
) {
    @Serializable
    internal class Candidate(
        val id: Long,
        @SerialName("accesskey") val accessKey: String,
        val duration: Long
    )
}
