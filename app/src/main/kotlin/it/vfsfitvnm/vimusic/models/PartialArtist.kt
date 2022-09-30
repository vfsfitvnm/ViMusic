package it.vfsfitvnm.vimusic.models

data class PartialArtist(
    val id: String,
    val name: String?,
    val thumbnailUrl: String?,
    val info: String?,
    val timestamp: Long? = null,
)
