package it.vfsfitvnm.vimusic.models

import androidx.room.Relation

class DetailedSongWithContentLength(
    id: String,
    title: String,
    artistsText: String? = null,
    durationText: String,
    thumbnailUrl: String?,
    likedAt: Long? = null,
    totalPlayTimeMs: Long = 0,
    albumId: String?,
    artists: List<Artist>?,
    @Relation(
        entity = Format::class,
        entityColumn = "songId",
        parentColumn = "id",
        projection = ["contentLength"]
    )
    val contentLength: Long?
) : DetailedSong(id, title, artistsText, durationText, thumbnailUrl, likedAt, totalPlayTimeMs, albumId, artists)
