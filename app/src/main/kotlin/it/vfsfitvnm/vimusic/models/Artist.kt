package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Artist(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUrl: String?,
    val info: String?,
    val shuffleVideoId: String? = null,
    val shufflePlaylistId: String? = null,
    val radioVideoId: String? = null,
    val radioPlaylistId: String? = null,
    val timestamp: Long?
)
