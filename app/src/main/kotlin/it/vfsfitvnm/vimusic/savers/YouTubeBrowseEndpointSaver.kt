package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint

object YouTubeBrowseEndpointSaver : Saver<NavigationEndpoint.Endpoint.Browse, List<Any?>> {
    override fun SaverScope.save(value: NavigationEndpoint.Endpoint.Browse) = listOf(
        value.browseId,
        value.params
    )

    override fun restore(value: List<Any?>) = NavigationEndpoint.Endpoint.Browse(
        browseId = value[0] as String,
        params = value[1] as String?,
        browseEndpointContextSupportedConfigs = null
    )
}
