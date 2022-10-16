package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsResponse(
    val contents: List<Content>?
) {
    @Serializable
    data class Content(
        val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer?
    ) {
        @Serializable
        data class SearchSuggestionsSectionRenderer(
            val contents: List<Content>?
        ) {
            @Serializable
            data class Content(
                val searchSuggestionRenderer: SearchSuggestionRenderer?
            ) {
                @Serializable
                data class SearchSuggestionRenderer(
                    val navigationEndpoint: NavigationEndpoint?,
                )
            }
        }
    }
}
