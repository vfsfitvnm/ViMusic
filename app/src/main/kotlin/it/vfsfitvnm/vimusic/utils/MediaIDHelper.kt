package it.vfsfitvnm.vimusic.utils

import androidx.media3.common.MediaItem
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.enums.MediaIDType
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MediaIDHelper {

    companion object {

        fun createMediaIdForSong(id: String): String {
            return MediaIDType.Song.prefix.plus(id)
        }

        fun createMediaIdForPlaylist(id: Long): String {
            return MediaIDType.Playlist.prefix.plus(id)
        }

        fun createMediaIdForRandomFavorites(): String {
            return MediaIDType.RandomFavorites.prefix
        }

        fun createMediaIdForRandomSongs(): String {
            return MediaIDType.RandomSongs.prefix
        }

        fun extractMusicQueueFromMediaId(mediaID: String?): List<MediaItem> {
            val result = mutableListOf<MediaItem>()
            mediaID?.apply {
                with(mediaID) {
                    when {
                        startsWith(MediaIDType.Song.prefix) -> {
                            val id = mediaID.removePrefix(MediaIDType.Song.prefix)
                            val song = runBlocking(Dispatchers.IO) {
                                Database.songById(id).first()
                            }
                            song?.apply {
                                result.add(song.asMediaItem)
                            }
                        }
                        startsWith(MediaIDType.Playlist.prefix) -> {
                            val id = mediaID.removePrefix(MediaIDType.Playlist.prefix).toLong()
                            val playlist = runBlocking(Dispatchers.IO) {
                                Database.playlistWithSongs(id).first()
                            }
                            playlist?.apply {
                                if (playlist.songs.isNotEmpty()) {
                                    playlist.songs.map { it.asMediaItem }.forEach(result::add)
                                }
                            }
                        }
                        startsWith(MediaIDType.RandomFavorites.prefix) -> {
                            val favorites = runBlocking(Dispatchers.IO) {
                                Database.favorites().first()
                            }
                            favorites.map { it.asMediaItem }.forEach(result::add)
                            result.shuffle()
                        }
                        startsWith(MediaIDType.RandomSongs.prefix) -> {
                            val favorites = runBlocking(Dispatchers.IO) {
                                Database.songs(SongSortBy.DateAdded, SortOrder.Descending).first()
                            }
                            favorites.map { it.asMediaItem }.forEach(result::add)
                            result.shuffle()
                        }
                    }
                }
            }
            return result
        }
    }
}