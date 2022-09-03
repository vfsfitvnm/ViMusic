package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Relation

@Immutable
class DetailedSongWithContentLength(
    id: String,
    title: String,
    artistsText: String? = null,
    durationText: String,
    thumbnailUrl: String?,
    totalPlayTimeMs: Long = 0,
    albumId: String?,
    artists: List<Info>?,
    @Relation(
        entity = Format::class,
        entityColumn = "songId",
        parentColumn = "id",
        projection = ["contentLength"]
    )
    val contentLength: Long?
) : DetailedSong(id, title, artistsText, durationText, thumbnailUrl, totalPlayTimeMs, albumId, artists)
