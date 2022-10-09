package it.vfsfitvnm.youtubemusic.models.bodies

import it.vfsfitvnm.youtubemusic.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context = Context.DefaultAndroid,
    val videoId: String,
    val playlistId: String? = null
)
