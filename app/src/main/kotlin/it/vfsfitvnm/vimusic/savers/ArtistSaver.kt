package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.Playlist

object ArtistSaver : Saver<Artist, List<Any?>> {
    override fun SaverScope.save(value: Artist): List<Any?> = listOf(
        value.id,
        value.name,
        value.thumbnailUrl,
        value.info,
        value.shuffleVideoId,
        value.shufflePlaylistId,
        value.radioVideoId,
        value.radioPlaylistId,
        value.timestamp,
        value.bookmarkedAt,
    )

    override fun restore(value: List<Any?>): Artist = Artist(
        id = value[0] as String,
        name = value[1] as String,
        thumbnailUrl = value[2] as String?,
        info = value[3] as String?,
        shuffleVideoId = value[4] as String?,
        shufflePlaylistId = value[5] as String?,
        radioVideoId = value[6] as String?,
        radioPlaylistId = value[7] as String?,
        timestamp = value[8] as Long?,
        bookmarkedAt = value[9] as Long?,
    )
}
