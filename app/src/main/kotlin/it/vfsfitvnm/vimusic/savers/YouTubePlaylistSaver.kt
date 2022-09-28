package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubePlaylistSaver : Saver<YouTube.Item.Playlist, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Item.Playlist): List<Any?> = listOf(
        value.info?.let { with(YouTubeBrowseInfoSaver) { save(it) } },
        value.channel?.let { with(YouTubeBrowseInfoSaver) { save(it) } },
        value.songCount,
        value.thumbnail?.let { with(YouTubeThumbnailSaver) { save(it) } }
    )

    override fun restore(value: List<Any?>) = YouTube.Item.Playlist(
        info = (value[0] as List<Any?>?)?.let(YouTubeBrowseInfoSaver::restore),
        channel = (value[1] as List<Any?>?)?.let(YouTubeBrowseInfoSaver::restore),
        songCount = value[2] as Int?,
        thumbnail = (value[3] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore)
    )
}
