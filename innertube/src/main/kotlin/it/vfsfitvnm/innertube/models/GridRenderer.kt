package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class GridRenderer(
    val items: List<Item>?,
) {
    @Serializable
    data class Item(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?
    )
}
