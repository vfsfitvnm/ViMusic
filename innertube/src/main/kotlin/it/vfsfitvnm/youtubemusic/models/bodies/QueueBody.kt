package it.vfsfitvnm.youtubemusic.models.bodies

import it.vfsfitvnm.youtubemusic.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class QueueBody(
    val context: Context = Context.DefaultWeb,
    val videoIds: List<String>? = null,
    val playlistId: String? = null,
)
