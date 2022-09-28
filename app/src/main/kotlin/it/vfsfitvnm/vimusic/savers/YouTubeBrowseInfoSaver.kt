package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint

object YouTubeBrowseInfoSaver : Saver<YouTube.Info<NavigationEndpoint.Endpoint.Browse>, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Info<NavigationEndpoint.Endpoint.Browse>) = listOf(
        value.name,
        value.endpoint?.let { with(YouTubeBrowseEndpointSaver) { save(it) } }
    )

    override fun restore(value: List<Any?>) = YouTube.Info(
        name = value[0] as String?,
        endpoint = (value[1] as List<Any?>?)?.let(YouTubeBrowseEndpointSaver::restore)
    )
}
