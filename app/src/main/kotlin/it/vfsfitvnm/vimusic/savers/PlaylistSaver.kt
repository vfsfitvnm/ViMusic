package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Playlist

object PlaylistSaver : Saver<Playlist, List<Any?>> {
    override fun SaverScope.save(value: Playlist): List<Any?> = listOf(
        value.id,
        value.name,
        value.browseId,
    )

    override fun restore(value: List<Any?>): Playlist = Playlist(
        id = value[0] as Long,
        name = value[1] as String,
        browseId = value[2] as String?,
    )
}
