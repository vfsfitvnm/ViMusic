package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Tabs(
    val tabs: List<Tab>?
) {
    @Serializable
    data class Tab(
        val tabRenderer: TabRenderer?
    ) {
        @Serializable
        data class TabRenderer(
            val content: Content?,
            val title: String?,
            val tabIdentifier: String?,
        ) {
            @Serializable
            data class Content(
                val sectionListRenderer: SectionListRenderer?,
            )
        }
    }
}
