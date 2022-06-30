package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class DetailedSong(
    @Embedded val song: Song,
    @Relation(
        entity = SongAlbumMap::class,
        entityColumn = "songId",
        parentColumn = "id"
    )
    val albumId: String?,
    @Relation(
        entity = Artist::class,
        entityColumn = "id",
        parentColumn = "id",
        associateBy = Junction(
            value = SongArtistMap::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<Artist>?
)
