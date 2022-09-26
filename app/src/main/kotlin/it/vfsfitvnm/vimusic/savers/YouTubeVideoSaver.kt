package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeVideoSaver : Saver<YouTube.Item.Video, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Video): List<Any?> = listOf(
        with(YouTubeWatchInfoSaver) { save(value.info) },
        with(YouTubeBrowseInfoListSaver) { value.authors?.let { save(it) } },
        value.viewsText,
        value.durationText,
        with(YouTubeThumbnailSaver) { value.thumbnail?.let { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Item.Video(
        info = YouTubeWatchInfoSaver.restore(value[0] as List<Any?>),
        authors = YouTubeBrowseInfoListSaver.restore(value[1] as List<List<Any?>>),
        viewsText = value[2] as String?,
        durationText = value[3] as String?,
        thumbnail = (value[4] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
