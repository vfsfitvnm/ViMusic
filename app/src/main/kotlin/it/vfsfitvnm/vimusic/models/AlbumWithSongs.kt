package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Immutable
data class AlbumWithSongs(
    @Embedded val album: Album,
    @Relation(
        entity = Song::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SortedSongAlbumMap::class,
            parentColumn = "albumId",
            entityColumn = "songId"
        )
    )
    val songs: List<DetailedSong>
)
