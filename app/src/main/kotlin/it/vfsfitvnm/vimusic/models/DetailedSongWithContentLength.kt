package it.vfsfitvnm.vimusic.models

import androidx.room.Relation

class DetailedSongWithContentLength(
    song: Song,
    albumId: String?,
    artists: List<Artist>?,
    @Relation(
        entity = Format::class,
        entityColumn = "songId",
        parentColumn = "id",
        projection = ["contentLength"]
    )
    val contentLength: Long?
) : DetailedSong(song, albumId, artists)
