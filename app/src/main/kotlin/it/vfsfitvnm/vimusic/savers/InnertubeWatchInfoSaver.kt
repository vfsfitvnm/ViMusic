package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.NavigationEndpoint

object InnertubeWatchInfoSaver : Saver<Innertube.Info<NavigationEndpoint.Endpoint.Watch>, List<Any?>> {
    override fun SaverScope.save(value: Innertube.Info<NavigationEndpoint.Endpoint.Watch>) = listOf(
        value.name,
        value.endpoint?.let { with(InnertubeWatchEndpointSaver) { save(it) } },
    )

    override fun restore(value: List<Any?>) = Innertube.Info(
        name = value[0] as String?,
        endpoint = (value[1] as List<Any?>?)?.let(InnertubeWatchEndpointSaver::restore)
    )
}
