package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        entity = Song::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SortedSongPlaylistMap::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<DetailedSong>
) {
    companion object {
        val Empty = PlaylistWithSongs(Playlist(-1, ""), emptyList())
        val NotFound = PlaylistWithSongs(Playlist(-2, "Not found"), emptyList())
    }
}
