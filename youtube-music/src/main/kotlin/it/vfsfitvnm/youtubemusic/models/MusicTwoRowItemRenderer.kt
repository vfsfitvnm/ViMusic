package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint,
    val thumbnailRenderer: ThumbnailRenderer,
    val title: Runs,
    val subtitle: Runs,
)
