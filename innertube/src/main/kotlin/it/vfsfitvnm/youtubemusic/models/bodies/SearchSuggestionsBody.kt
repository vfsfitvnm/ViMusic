package it.vfsfitvnm.youtubemusic.models.bodies

import it.vfsfitvnm.youtubemusic.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsBody(
    val context: Context = Context.DefaultWeb,
    val input: String
)
