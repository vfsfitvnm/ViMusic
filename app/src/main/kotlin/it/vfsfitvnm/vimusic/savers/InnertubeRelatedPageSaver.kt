package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeRelatedPageSaver : Saver<Innertube.RelatedPage, List<Any?>> {
    override fun SaverScope.save(value: Innertube.RelatedPage): List<Any?> = listOf(
        value.songs?.let { with(InnertubeSongItemListSaver) { save(it) } },
        value.playlists?.let { with(InnertubePlaylistItemListSaver) { save(it) } },
        value.albums?.let { with(InnertubeAlbumItemListSaver) { save(it) } },
        value.artists?.let { with(InnertubeArtistItemListSaver) { save(it) } },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.RelatedPage(
        songs = (value[0] as List<List<Any?>>?)?.let(InnertubeSongItemListSaver::restore),
        playlists = (value[1] as List<List<Any?>>?)?.let(InnertubePlaylistItemListSaver::restore),
        albums = (value[2] as List<List<Any?>>?)?.let(InnertubeAlbumItemListSaver::restore),
        artists = (value[3] as List<List<Any?>>?)?.let(InnertubeArtistItemListSaver::restore),
    )
}
