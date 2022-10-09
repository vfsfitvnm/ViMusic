package it.vfsfitvnm.youtubemusic.models.bodies

import it.vfsfitvnm.youtubemusic.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String,
)
