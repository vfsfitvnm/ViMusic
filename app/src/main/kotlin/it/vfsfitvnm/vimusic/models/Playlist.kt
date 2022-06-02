package it.vfsfitvnm.vimusic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
) {
    companion object {
        val Empty = Playlist(
            id = 0,
            name = ""
        )
    }
}
