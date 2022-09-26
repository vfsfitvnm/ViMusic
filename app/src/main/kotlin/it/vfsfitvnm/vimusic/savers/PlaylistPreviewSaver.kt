package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.PlaylistPreview

object PlaylistPreviewSaver : Saver<PlaylistPreview, List<Any?>> {
    override fun SaverScope.save(value: PlaylistPreview): List<Any> {
        return listOf(
            with(PlaylistSaver) { save(value.playlist) },
            value.songCount,
        )
    }

    override fun restore(value: List<Any?>): PlaylistPreview? {
        return if (value.size == 2) PlaylistPreview(
            playlist = PlaylistSaver.restore(value[0] as List<Any?>),
            songCount = value[1] as Int,
        ) else null
    }
}
