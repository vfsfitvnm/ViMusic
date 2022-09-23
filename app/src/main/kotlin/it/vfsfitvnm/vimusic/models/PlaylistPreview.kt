package it.vfsfitvnm.vimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class PlaylistPreview(
    @Embedded val playlist: Playlist,
    val songCount: Int
)

sealed interface PlaylistItem {
    val contentId: Long
    val title: String
}

@Immutable
data class RealPlaylistItem(
    val playlistPreview: PlaylistPreview
) : PlaylistItem {

    override val contentId: Long = playlistPreview.playlist.id
    override val title: String = playlistPreview.playlist.name
}

@Immutable
object FavoritePlaylistItem : PlaylistItem {
    override val contentId: Long = -1
    override val title: String = "Favorites"
}

@Immutable
object OfflinePlaylistItem : PlaylistItem {
    override val contentId: Long = -2
    override val title: String = "Offline"
}
