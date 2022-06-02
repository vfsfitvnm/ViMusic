package it.vfsfitvnm.vimusic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// I know...
@Entity
data class Info(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val browseId: String?,
    val text: String
)
