package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@Immutable
@DatabaseView("SELECT * FROM SongAlbumMap ORDER BY position")
data class SortedSongAlbumMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val albumId: String,
    val position: Int
)
