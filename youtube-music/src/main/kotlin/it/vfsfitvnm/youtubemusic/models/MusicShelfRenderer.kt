package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable

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
            get() = (musicResponsiveListItemRenderer.flexColumns.first().musicResponsiveListItemFlexColumnRenderer.text?.runs ?: emptyList()) to
                    (musicResponsiveListItemRenderer.flexColumns.last().musicResponsiveListItemFlexColumnRenderer.text?.splitBySeparator() ?: emptyList())

        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail
            get() = musicResponsiveListItemRenderer.thumbnail!!.musicThumbnailRenderer.thumbnail.thumbnails.first()
    }
}
