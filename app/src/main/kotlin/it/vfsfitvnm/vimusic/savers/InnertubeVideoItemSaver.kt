package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeVideoItemSaver : Saver<Innertube.VideoItem, List<Any?>> {
    override fun SaverScope.save(value: Innertube.VideoItem): List<Any?> = listOf(
        value.info?.let { with(InnertubeWatchInfoSaver) { save(it) } },
        value.authors?.let { with(InnertubeBrowseInfoListSaver) { save(it) } },
        value.viewsText,
        value.durationText,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.VideoItem(
        info = (value[0] as List<Any?>?)?.let(InnertubeWatchInfoSaver::restore),
        authors = (value[1] as List<List<Any?>>?)?.let(InnertubeBrowseInfoListSaver::restore),
        viewsText = value[2] as String?,
        durationText = value[3] as String?,
        thumbnail = (value[4] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore)
    )
}

val InnertubeVideoItemListSaver = listSaver(InnertubeVideoItemSaver)
