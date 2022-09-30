package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeArtistPageSaver : Saver<YouTube.Artist, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Artist): List<Any?> = listOf(
        value.name,
        value.description,
        value.thumbnail?.let { with(YouTubeThumbnailSaver) { save(it) } },
        value.shuffleEndpoint?.let { with(YouTubeWatchEndpointSaver) { save(it) } },
        value.radioEndpoint?.let { with(YouTubeWatchEndpointSaver) { save(it) } },
        value.songs?.let { with(YouTubeSongListSaver) { save(it) } },
        value.songsEndpoint?.let { with(YouTubeBrowseEndpointSaver) { save(it) } },
        value.albums?.let { with(YouTubeAlbumListSaver) { save(it) } },
        value.albumsEndpoint?.let { with(YouTubeBrowseEndpointSaver) { save(it) } },
        value.singles?.let { with(YouTubeAlbumListSaver) { save(it) } },
        value.singlesEndpoint?.let { with(YouTubeBrowseEndpointSaver) { save(it) } },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Artist(
        name = value[0] as String?,
        description = value[1] as String?,
        thumbnail = (value[2] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore),
        shuffleEndpoint = (value[3] as List<Any?>?)?.let(YouTubeWatchEndpointSaver::restore),
        radioEndpoint = (value[4] as List<Any?>?)?.let(YouTubeWatchEndpointSaver::restore),
        songs = (value[5] as List<List<Any?>>?)?.let(YouTubeSongListSaver::restore),
        songsEndpoint = (value[6] as List<Any?>?)?.let(YouTubeBrowseEndpointSaver::restore),
        albums = (value[7] as List<List<Any?>>?)?.let(YouTubeAlbumListSaver::restore),
        albumsEndpoint = (value[8] as List<Any?>?)?.let(YouTubeBrowseEndpointSaver::restore),
        singles = (value[9] as List<List<Any?>>?)?.let(YouTubeAlbumListSaver::restore),
        singlesEndpoint = (value[10] as List<Any?>?)?.let(YouTubeBrowseEndpointSaver::restore),
    )
}
