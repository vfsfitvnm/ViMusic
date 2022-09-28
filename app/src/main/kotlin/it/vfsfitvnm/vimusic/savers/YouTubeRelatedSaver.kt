package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubeRelatedSaver : Saver<YouTube.Related, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Related): List<Any?> = listOf(
        value.songs?.let { with(YouTubeSongListSaver) { save(it) } },
        value.playlists?.let { with(YouTubePlaylistListSaver) { save(it) } },
        value.albums?.let { with(YouTubeAlbumListSaver) { save(it) } },
        value.artists?.let { with(YouTubeArtistListSaver) { save(it) } },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.Related(
        songs = (value[0] as List<List<Any?>>?)?.let(YouTubeSongListSaver::restore),
        playlists = (value[1] as List<List<Any?>>?)?.let(YouTubePlaylistListSaver::restore),
        albums = (value[2] as List<List<Any?>>?)?.let(YouTubeAlbumListSaver::restore),
        artists = (value[3] as List<List<Any?>>?)?.let(YouTubeArtistListSaver::restore),
    )
}
