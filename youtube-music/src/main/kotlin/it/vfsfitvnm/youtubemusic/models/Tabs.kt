package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Tabs(
    val tabs: List<Tab>
) {
    @Serializable
    data class Tab(
        val tabRenderer: TabRenderer
    ) {
        @Serializable
        data class TabRenderer(
            val content: Content?,
            val title: String?,
            val tabIdentifier: String?,
        ) {
            @Serializable
            data class Content(
                val sectionListRenderer: SectionListRenderer,
            )
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SectionListRenderer(
    val contents: List<Content>,
    val continuations: List<Continuation>?
) {
    @Serializable
    data class Content(
        @JsonNames("musicImmersiveCarouselShelfRenderer")
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
        @JsonNames("musicPlaylistShelfRenderer")
        val musicShelfRenderer: MusicShelfRenderer?,
        val gridRenderer: GridRenderer?,
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
    ) {
        @Serializable
        data class GridRenderer(
            val items: List<Item>,
        ) {
            @Serializable
            data class Item(
                val musicNavigationButtonRenderer: MusicNavigationButtonRenderer
            )
        }

        @Serializable
        data class MusicDescriptionShelfRenderer(
            val description: Runs,
        )
    }
}
