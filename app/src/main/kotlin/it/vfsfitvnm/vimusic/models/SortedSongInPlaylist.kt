package it.vfsfitvnm.vimusic.models

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView("SELECT * FROM SongInPlaylist ORDER BY position")
data class SortedSongInPlaylist(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int
)
