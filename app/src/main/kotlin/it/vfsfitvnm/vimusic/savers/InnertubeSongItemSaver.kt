package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeSongItemSaver : Saver<Innertube.SongItem, List<Any?>> {
    override fun SaverScope.save(value: Innertube.SongItem): List<Any?> = listOf(
        value.info?.let { with(InnertubeWatchInfoSaver) { save(it) } },
        value.authors?.let { with(InnertubeBrowseInfoListSaver) { save(it) } },
        value.album?.let { with(InnertubeBrowseInfoSaver) { save(it) } },
        value.durationText,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.SongItem(
        info = (value[0] as List<Any?>?)?.let(InnertubeWatchInfoSaver::restore),
        authors = (value[1] as List<List<Any?>>?)?.let(InnertubeBrowseInfoListSaver::restore),
        album = (value[2] as List<Any?>?)?.let(InnertubeBrowseInfoSaver::restore),
        durationText = value[3] as String?,
        thumbnail = (value[4] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore)
    )
}

val InnertubeSongItemListSaver = listSaver(InnertubeSongItemSaver)
