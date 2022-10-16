package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubePlaylistOrAlbumPageSaver : Saver<Innertube.PlaylistOrAlbumPage, List<Any?>> {
    override fun SaverScope.save(value: Innertube.PlaylistOrAlbumPage): List<Any?> = listOf(
        value.title,
        value.authors?.let { with(InnertubeBrowseInfoListSaver) { save(it) } },
        value.year,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } } ,
        value.url,
        value.songsPage?.let { with(InnertubeSongsPageSaver) { save(it) } },
        value.otherVersions?.let { with(InnertubeAlbumItemListSaver) { save(it) } },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.PlaylistOrAlbumPage(
        title = value[0] as String?,
        authors = (value[1] as List<List<Any?>>?)?.let(InnertubeBrowseInfoListSaver::restore),
        year = value[2] as String?,
        thumbnail = (value[3] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore),
        url = value[4] as String?,
        songsPage = (value[5] as List<Any?>?)?.let(InnertubeSongsPageSaver::restore),
        otherVersions = (value[6] as List<List<Any?>>?)?.let(InnertubeAlbumItemListSaver::restore),
    )
}
