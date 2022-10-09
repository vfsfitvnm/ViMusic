package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.PlaylistPreview

object PlaylistPreviewSaver : Saver<PlaylistPreview, List<Any>> {
    override fun SaverScope.save(value: PlaylistPreview) = listOf(
        with(PlaylistSaver) { save(value.playlist) },
        value.songCount,
    )

    override fun restore(value: List<Any>) = PlaylistPreview(
        playlist = PlaylistSaver.restore(value[0] as List<Any?>),
        songCount = value[1] as Int,
    )
}

val PlaylistPreviewListSaver = listSaver(PlaylistPreviewSaver)
