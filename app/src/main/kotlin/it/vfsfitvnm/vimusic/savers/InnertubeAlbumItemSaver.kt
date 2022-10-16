package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeAlbumItemSaver : Saver<Innertube.AlbumItem, List<Any?>> {
    override fun SaverScope.save(value: Innertube.AlbumItem): List<Any?> = listOf(
        value.info?.let { with(InnertubeBrowseInfoSaver) { save(it) } },
        value.authors?.let { with(InnertubeBrowseInfoListSaver) { save(it) } },
        value.year,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } }
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.AlbumItem(
        info = (value[0] as List<Any?>?)?.let(InnertubeBrowseInfoSaver::restore),
        authors = (value[1] as List<List<Any?>>?)?.let(InnertubeBrowseInfoListSaver::restore),
        year = value[2] as String?,
        thumbnail = (value[3] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore)
    )
}

val InnertubeAlbumItemListSaver = listSaver(InnertubeAlbumItemSaver)
