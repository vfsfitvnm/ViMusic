package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeAlbumSaver : Saver<YouTube.Item.Album, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Album): List<Any?> = listOf(
        with(YouTubeBrowseInfoSaver) { save(value.info) },
        with(YouTubeBrowseInfoListSaver) { value.authors?.let { save(it) } },
        value.year,
        with(YouTubeThumbnailSaver) { value.thumbnail?.let { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Item.Album(
        info = YouTubeBrowseInfoSaver.restore(value[0] as List<Any?>),
        authors = (value[1] as List<List<Any?>>?)?.let(YouTubeBrowseInfoListSaver::restore),
        year = value[2] as String?,
        thumbnail = (value[3] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
