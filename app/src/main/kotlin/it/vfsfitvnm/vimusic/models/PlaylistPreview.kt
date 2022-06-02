package it.vfsfitvnm.vimusic.models

import androidx.room.Embedded

data class PlaylistPreview(
    @Embedded val playlist: Playlist,
    val songCount: Int
)
