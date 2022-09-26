package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeArtistSaver : Saver<YouTube.Item.Artist, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Artist): List<Any?> = listOf(
        with(YouTubeBrowseInfoSaver) { save(value.info) },
        value.subscribersCountText,
        with(YouTubeThumbnailSaver) { value.thumbnail?.let { save(it) } }
    )

    override fun restore(value: List<Any?>) = YouTube.Item.Artist(
        info = YouTubeBrowseInfoSaver.restore(value[0] as List<Any?>),
        subscribersCountText = value[1] as String?,
        thumbnail = (value[2] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
