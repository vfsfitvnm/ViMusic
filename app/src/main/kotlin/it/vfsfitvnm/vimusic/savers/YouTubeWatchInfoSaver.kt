package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint

object YouTubeWatchInfoSaver : Saver<YouTube.Info<NavigationEndpoint.Endpoint.Watch>, List<Any?>> {
    override fun SaverScope.save(value: YouTube.Info<NavigationEndpoint.Endpoint.Watch>) = listOf(
        value.name,
        value.endpoint?.let { with(YouTubeWatchEndpointSaver) { save(it) } },
    )

    override fun restore(value: List<Any?>) = YouTube.Info(
        name = value[0] as String?,
        endpoint = (value[1] as List<Any?>?)?.let(YouTubeWatchEndpointSaver::restore)
    )
}
