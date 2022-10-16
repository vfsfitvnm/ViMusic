package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeArtistPageSaver : Saver<Innertube.ArtistPage, List<Any?>> {
    override fun SaverScope.save(value: Innertube.ArtistPage) = listOf(
        value.name,
        value.description,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } },
        value.shuffleEndpoint?.let { with(InnertubeWatchEndpointSaver) { save(it) } },
        value.radioEndpoint?.let { with(InnertubeWatchEndpointSaver) { save(it) } },
        value.songs?.let { with(InnertubeSongItemListSaver) { save(it) } },
        value.songsEndpoint?.let { with(InnertubeBrowseEndpointSaver) { save(it) } },
        value.albums?.let { with(InnertubeAlbumItemListSaver) { save(it) } },
        value.albumsEndpoint?.let { with(InnertubeBrowseEndpointSaver) { save(it) } },
        value.singles?.let { with(InnertubeAlbumItemListSaver) { save(it) } },
        value.singlesEndpoint?.let { with(InnertubeBrowseEndpointSaver) { save(it) } },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = Innertube.ArtistPage(
        name = value[0] as String?,
        description = value[1] as String?,
        thumbnail = (value[2] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore),
        shuffleEndpoint = (value[3] as List<Any?>?)?.let(InnertubeWatchEndpointSaver::restore),
        radioEndpoint = (value[4] as List<Any?>?)?.let(InnertubeWatchEndpointSaver::restore),
        songs = (value[5] as List<List<Any?>>?)?.let(InnertubeSongItemListSaver::restore),
        songsEndpoint = (value[6] as List<Any?>?)?.let(InnertubeBrowseEndpointSaver::restore),
        albums = (value[7] as List<List<Any?>>?)?.let(InnertubeAlbumItemListSaver::restore),
        albumsEndpoint = (value[8] as List<Any?>?)?.let(InnertubeBrowseEndpointSaver::restore),
        singles = (value[9] as List<List<Any?>>?)?.let(InnertubeAlbumItemListSaver::restore),
        singlesEndpoint = (value[10] as List<Any?>?)?.let(InnertubeBrowseEndpointSaver::restore),
    )
}
