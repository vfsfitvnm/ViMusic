package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.DetailedSong

object DetailedSongSaver : Saver<DetailedSong, List<Any?>> {
    override fun SaverScope.save(value: DetailedSong) =
        listOf(
            value.id,
            value.title,
            value.artistsText,
            value.durationText,
            value.thumbnailUrl,
            value.totalPlayTimeMs,
            value.albumId,
            value.artists?.let { with(InfoListSaver) { save(it) } }
        )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = DetailedSong(
        id = value[0] as String,
        title = value[1] as String,
        artistsText = value[2] as String?,
        durationText = value[3] as String?,
        thumbnailUrl = value[4] as String?,
        totalPlayTimeMs = value[5] as Long,
        albumId = value[6] as String?,
        artists = (value[7] as List<List<String>>?)?.let(InfoListSaver::restore)
    )
}

val DetailedSongListSaver = listSaver(DetailedSongSaver)
