package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MusicShelfRenderer(
    val bottomEndpoint: NavigationEndpoint?,
    val contents: List<Content>,
    val continuations: List<Continuation>?,
    val title: Runs?
) {
    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer,
    ) {
        val runs: Pair<List<Runs.Run>, List<List<Runs.Run>>>
            get() = (musicResponsiveListItemRenderer
                .flexColumns
                .firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?: emptyList()) to
                    (musicResponsiveListItemRenderer
                        .flexColumns
                        .lastOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.splitBySeparator()
                        ?: emptyList()
                            )

        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
            get() = musicResponsiveListItemRenderer
                .thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.firstOrNull()
    }
}
