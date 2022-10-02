package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Artist

object ArtistSaver : Saver<Artist, List<Any?>> {
    override fun SaverScope.save(value: Artist): List<Any?> = listOf(
        value.id,
        value.name,
        value.thumbnailUrl,
        value.info,
        value.timestamp,
        value.bookmarkedAt,
    )

    override fun restore(value: List<Any?>): Artist = Artist(
        id = value[0] as String,
        name = value[1] as String,
        thumbnailUrl = value[2] as String?,
        info = value[3] as String?,
        timestamp = value[4] as Long?,
        bookmarkedAt = value[5] as Long?,
    )
}

val ArtistListSaver = listSaver(ArtistSaver)
