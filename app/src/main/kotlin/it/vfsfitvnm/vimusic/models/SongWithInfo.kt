package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

open class SongWithInfo(
    @Embedded val song: Song,
    @Relation(
        entity = Info::class,
        parentColumn = "albumInfoId",
        entityColumn = "id"
    ) val album: Info?,
    @Relation(
        entity = Info::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SongWithAuthors::class,
            parentColumn = "songId",
            entityColumn = "authorInfoId"
        )
    )
    val authors: List<Info>?
)
