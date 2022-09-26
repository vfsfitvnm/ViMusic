package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeSongSaver : Saver<YouTube.Item.Song, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Song): List<Any?> = listOf(
        with(YouTubeWatchInfoSaver) { save(value.info) },
        with(YouTubeBrowseInfoListSaver) { value.authors?.let { save(it) } },
        with(YouTubeBrowseInfoSaver) { value.album?.let { save(it) } },
        value.durationText,
        with(YouTubeThumbnailSaver) { value.thumbnail?.let { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Item.Song(
        info = YouTubeWatchInfoSaver.restore(value[0] as List<Any?>),
        authors = YouTubeBrowseInfoListSaver.restore(value[1] as List<List<Any?>>),
        album = (value[2] as List<Any?>?)?.let(YouTubeBrowseInfoSaver::restore),
        durationText = value[3] as String?,
        thumbnail = (value[4] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
