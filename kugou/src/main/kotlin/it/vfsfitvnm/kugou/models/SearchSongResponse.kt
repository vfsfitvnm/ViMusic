package it.vfsfitvnm.kugou.models

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchSongResponse(
    val data: Data
) {
    @Serializable
    internal data  class Data(
        val info: List<Info>
    ) {
        @Serializable
        internal data class Info(
            val duration: Long,
            val hash: String
        )
    }
}
