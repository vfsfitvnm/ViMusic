package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.models.ThumbnailRenderer

object YouTubeThumbnailSaver : Saver<ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail, List<Any?>> {
    override fun SaverScope.save(value: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail) = listOf(
        value.url,
        value.width,
        value.height
    )

    override fun restore(value: List<Any?>) = ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail(
        url = value[0] as String,
        width = value[1] as Int,
        height = value[2] as Int?,
    )
}
