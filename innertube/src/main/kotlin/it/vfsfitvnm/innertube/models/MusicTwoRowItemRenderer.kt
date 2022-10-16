package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint?,
    val thumbnailRenderer: ThumbnailRenderer?,
    val title: Runs?,
    val subtitle: Runs?,
)
