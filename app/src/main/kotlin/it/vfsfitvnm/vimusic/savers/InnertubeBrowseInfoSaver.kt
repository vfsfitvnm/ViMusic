package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.NavigationEndpoint

object InnertubeBrowseInfoSaver : Saver<Innertube.Info<NavigationEndpoint.Endpoint.Browse>, List<Any?>> {
    override fun SaverScope.save(value: Innertube.Info<NavigationEndpoint.Endpoint.Browse>) = listOf(
        value.name,
        value.endpoint?.let { with(InnertubeBrowseEndpointSaver) { save(it) } }
    )

    override fun restore(value: List<Any?>) = Innertube.Info(
        name = value[0] as String?,
        endpoint = (value[1] as List<Any?>?)?.let(InnertubeBrowseEndpointSaver::restore)
    )
}

val InnertubeBrowseInfoListSaver = listSaver(InnertubeBrowseInfoSaver)
