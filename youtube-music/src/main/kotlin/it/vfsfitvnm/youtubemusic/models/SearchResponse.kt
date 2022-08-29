package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SearchResponse(
    val contents: Contents,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: Tabs
    )
}
