package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubePlaylistItemSaver : Saver<Innertube.PlaylistItem, List<Any?>> {
    override fun SaverScope.save(value: Innertube.PlaylistItem): List<Any?> = listOf(
        value.info?.let { with(InnertubeBrowseInfoSaver) { save(it) } },
        value.channel?.let { with(InnertubeBrowseInfoSaver) { save(it) } },
        value.songCount,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } }
    )

    override fun restore(value: List<Any?>) = Innertube.PlaylistItem(
        info = (value[0] as List<Any?>?)?.let(InnertubeBrowseInfoSaver::restore),
        channel = (value[1] as List<Any?>?)?.let(InnertubeBrowseInfoSaver::restore),
        songCount = value[2] as Int?,
        thumbnail = (value[3] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore)
    )
}

val InnertubePlaylistItemListSaver = listSaver(InnertubePlaylistItemSaver)
