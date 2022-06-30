package it.vfsfitvnm.vimusic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Album(
    @PrimaryKey val id: String,
    val title: String?,
    val thumbnailUrl: String?,
    val year: String?,
    val authorsText: String?,
    val shareUrl: String?
)
