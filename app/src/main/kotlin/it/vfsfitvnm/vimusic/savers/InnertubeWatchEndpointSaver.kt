package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.models.NavigationEndpoint

object InnertubeWatchEndpointSaver : Saver<NavigationEndpoint.Endpoint.Watch, List<Any?>> {
    override fun SaverScope.save(value: NavigationEndpoint.Endpoint.Watch) = listOf(
        value.params,
        value.playlistId,
        value.videoId,
        value.index,
        value.playlistSetVideoId,
    )

    override fun restore(value: List<Any?>) = NavigationEndpoint.Endpoint.Watch(
        params = value[0] as String?,
        playlistId = value[1] as String?,
        videoId = value[2] as String?,
        index = value[3] as Int?,
        playlistSetVideoId = value[4] as String?,
        watchEndpointMusicSupportedConfigs = null
    )
}
