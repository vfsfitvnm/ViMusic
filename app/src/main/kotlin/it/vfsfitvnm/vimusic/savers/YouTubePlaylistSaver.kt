package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubePlaylistSaver : Saver<YouTube.Item.Playlist, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Playlist): List<Any?> = listOf(
        with(YouTubeBrowseInfoSaver) { save(value.info) },
        with(YouTubeBrowseInfoSaver) { value.channel?.let { save(it) } },
        value.songCount,
        with(YouTubeThumbnailSaver) { value.thumbnail?.let { save(it) } }
    )

    override fun restore(value: List<Any?>) = YouTube.Item.Playlist(
        info = YouTubeBrowseInfoSaver.restore(value[0] as List<Any?>),
        channel = (value[1] as List<Any?>?)?.let(YouTubeBrowseInfoSaver::restore),
        songCount = value[2] as Int?,
        thumbnail = (value[3] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
