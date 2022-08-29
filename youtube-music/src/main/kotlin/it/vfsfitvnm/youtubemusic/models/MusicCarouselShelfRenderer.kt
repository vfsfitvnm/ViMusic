package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MusicCarouselShelfRenderer(
    val header: Header,
    val contents: List<Content>,
) {
    @Serializable
    data class Content(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?
    )

    @Serializable
    data class Header(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
    ) {
        @Serializable
        data class MusicCarouselShelfBasicHeaderRenderer(
            val moreContentButton: MoreContentButton?,
            val title: Runs,
        ) {
            @Serializable
            data class MoreContentButton(
                val buttonRenderer: ButtonRenderer
            )
        }
    }
}
