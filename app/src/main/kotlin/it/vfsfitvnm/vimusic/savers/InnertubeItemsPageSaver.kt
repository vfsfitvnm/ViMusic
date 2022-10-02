package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.Innertube

object InnertubeSongsPageSaver : Saver<Innertube.ItemsPage<Innertube.SongItem>, List<Any?>> {
    override fun SaverScope.save(value: Innertube.ItemsPage<Innertube.SongItem>) = listOf(
        value.items?.let {with(InnertubeSongItemListSaver) { save(it) } },
        value.continuation
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.ItemsPage(
        items = (value[0] as List<List<Any?>>?)?.let(InnertubeSongItemListSaver::restore),
        continuation = value[1] as String?
    )
}

object InnertubeAlbumsPageSaver : Saver<Innertube.ItemsPage<Innertube.AlbumItem>, List<Any?>> {
    override fun SaverScope.save(value: Innertube.ItemsPage<Innertube.AlbumItem>) = listOf(
        value.items?.let {with(InnertubeAlbumItemListSaver) { save(it) } },
        value.continuation
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.ItemsPage(
        items = (value[0] as List<List<Any?>>?)?.let(InnertubeAlbumItemListSaver::restore),
        continuation = value[1] as String?
    )
}
