package it.vfsfitvnm.vimusic.ui.screens.album

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.AlbumWithSongs
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.utils.toMediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AlbumViewModel(browseId: String) : ViewModel() {
    var result by mutableStateOf<Result<AlbumWithSongs?>?>(null)
        private set

    private var job: Job? = null

    init {
        fetch(browseId)
    }

    fun fetch(browseId: String) {
        job?.cancel()
        result = null

        job = viewModelScope.launch(Dispatchers.IO) {
            Database.albumWithSongs(browseId).collect { albumWithSongs ->
                result = if (albumWithSongs?.album?.timestamp == null) {
                    YouTube.album(browseId)?.map { youtubeAlbum ->
                        Database.upsert(
                            Album(
                                id = browseId,
                                title = youtubeAlbum.title,
                                thumbnailUrl = youtubeAlbum.thumbnail?.url,
                                year = youtubeAlbum.year,
                                authorsText = youtubeAlbum.authors?.joinToString("") { it.name },
                                shareUrl = youtubeAlbum.url,
                                timestamp = System.currentTimeMillis()
                            ),
                            youtubeAlbum.items?.mapIndexedNotNull { position, albumItem ->
                                albumItem.toMediaItem(browseId, youtubeAlbum)?.let { mediaItem ->
                                    Database.insert(mediaItem)
                                    SongAlbumMap(
                                        songId = mediaItem.mediaId,
                                        albumId = browseId,
                                        position = position
                                    )
                                }
                            } ?: emptyList()
                        )

                        null
                    }
                } else {
                    Result.success(albumWithSongs)
                }
            }
        }
    }
}
