package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class DetailedSong(
    @Embedded val song: Song,
    @Relation(
        entity = Album::class,
        parentColumn = "albumId",
        entityColumn = "id"
    ) val album: Album?,
    @Relation(
        entity = Artist::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SongArtistMap::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<Artist>?
)
