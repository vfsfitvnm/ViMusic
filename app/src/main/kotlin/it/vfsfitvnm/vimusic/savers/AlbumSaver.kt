package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Album

object AlbumSaver : Saver<Album, List<Any?>> {
    override fun SaverScope.save(value: Album): List<Any?> = listOf(
        value.id,
        value.title,
        value.thumbnailUrl,
        value.year,
        value.authorsText,
        value.shareUrl,
        value.timestamp,
        value.bookmarkedAt,
    )

    override fun restore(value: List<Any?>): Album = Album(
        id = value[0] as String,
        title = value[1] as String,
        thumbnailUrl = value[2] as String?,
        year = value[3] as String?,
        authorsText = value[4] as String?,
        shareUrl = value[5] as String?,
        timestamp = value[6] as Long?,
        bookmarkedAt = value[7] as Long?,
    )
}

val AlbumListSaver = listSaver(AlbumSaver)
