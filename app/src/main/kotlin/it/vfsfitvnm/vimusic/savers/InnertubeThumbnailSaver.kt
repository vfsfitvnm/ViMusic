package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.models.Thumbnail

object InnertubeThumbnailSaver : Saver<Thumbnail, List<Any?>> {
    override fun SaverScope.save(value: Thumbnail) = listOf(
        value.url,
        value.width,
        value.height
    )

    override fun restore(value: List<Any?>) = Thumbnail(
        url = value[0] as String,
        width = value[1] as Int,
        height = value[2] as Int?,
    )
}
