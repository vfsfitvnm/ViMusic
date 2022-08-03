package it.vfsfitvnm.vimusic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artistsText: String? = null,
    val durationText: String,
    val thumbnailUrl: String?,
    val lyrics: String? = null,
    val synchronizedLyrics: String? = null,
    val likedAt: Long? = null,
    val totalPlayTimeMs: Long = 0
) {
    fun toggleLike(): Song {
        return copy(
            likedAt = if (likedAt == null) System.currentTimeMillis() else null
        )
    }
}
