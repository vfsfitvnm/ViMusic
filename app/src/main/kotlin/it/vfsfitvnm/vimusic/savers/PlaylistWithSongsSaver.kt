package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.PlaylistWithSongs

object PlaylistWithSongsSaver : Saver<PlaylistWithSongs, List<Any>> {
    override fun SaverScope.save(value: PlaylistWithSongs) = listOf(
        with(PlaylistSaver) { save(value.playlist) },
        with(DetailedSongListSaver) { save(value.songs) },
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any>): PlaylistWithSongs = PlaylistWithSongs(
        playlist = PlaylistSaver.restore(value[0] as List<Any?>),
        songs = DetailedSongListSaver.restore(value[1] as List<List<Any?>>)
    )
}
