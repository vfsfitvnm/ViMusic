package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: Runs,
    val solid: Solid?,
    val clickCommand: ClickCommand,
) {
    @Serializable
    data class Solid(
        val leftStripeColor: Long
    )

    @Serializable
    data class ClickCommand(
        val clickTrackingParams: String,
        val browseEndpoint: NavigationEndpoint.Endpoint.Browse
    )
}