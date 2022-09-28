package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeAlbumSaver : Saver<YouTube.Item.Album, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Album): List<Any?> = listOf(
        value.info?.let { with(YouTubeBrowseInfoSaver) { save(it) } },
        value.authors?.let { with(YouTubeBrowseInfoListSaver) { save(it) } },
        value.year,
        value.thumbnail?.let { with(YouTubeThumbnailSaver) { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Item.Album(
        info = (value[0] as List<Any?>?)?.let(YouTubeBrowseInfoSaver::restore),
        authors = (value[1] as List<List<Any?>>?)?.let(YouTubeBrowseInfoListSaver::restore),
        year = value[2] as String?,
        thumbnail = (value[3] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
