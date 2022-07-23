package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

open class DetailedSong(
    @Embedded val song: Song,
    @Relation(
        entity = SongAlbumMap::class,
        entityColumn = "songId",
        parentColumn = "id",
        projection = ["albumId"]
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
