package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

object InnertubeArtistItemSaver : Saver<Innertube.ArtistItem, List<Any?>> {
    override fun SaverScope.save(value: Innertube.ArtistItem): List<Any?> = listOf(
        value.info?.let { with(InnertubeBrowseInfoSaver) { save(it) } },
        value.subscribersCountText,
        value.thumbnail?.let { with(InnertubeThumbnailSaver) { save(it) } }
    )

    override fun restore(value: List<Any?>) = Innertube.ArtistItem(
        info = (value[0] as List<Any?>?)?.let(InnertubeBrowseInfoSaver::restore),
        subscribersCountText = value[1] as String?,
        thumbnail = (value[2] as List<Any?>?)?.let(InnertubeThumbnailSaver::restore)
    )
}

val InnertubeArtistItemListSaver = listSaver(InnertubeArtistItemSaver)
