package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeVideoSaver : Saver<YouTube.Item.Video, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Video): List<Any?> = listOf(
        value.info?.let { with(YouTubeWatchInfoSaver) { save(it) } },
        value.authors?.let { with(YouTubeBrowseInfoListSaver) { save(it) } },
        value.viewsText,
        value.durationText,
        value.thumbnail?.let { with(YouTubeThumbnailSaver) { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Item.Video(
        info = (value[0] as List<Any?>?)?.let(YouTubeWatchInfoSaver::restore),
        authors = (value[1] as List<List<Any?>>?)?.let(YouTubeBrowseInfoListSaver::restore),
        viewsText = value[2] as String?,
        durationText = value[3] as String?,
        thumbnail = (value[4] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
