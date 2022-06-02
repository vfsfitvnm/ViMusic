package it.vfsfitvnm.youtubemusic.models


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ThumbnailRenderer(
    @JsonNames("croppedSquareThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer
) {
    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnail
    ) {
        @Serializable
        data class Thumbnail(
            val thumbnails: List<Thumbnail>
        ) {
            @Serializable
            data class Thumbnail(
                val url: String,
                val height: Int,
                val width: Int
            ) {
                val isResizable: Boolean
                    get() = !url.startsWith("https://i.ytimg.com")

                fun width(width: Int): String {
                    return when {
                        url.startsWith("https://lh3.googleusercontent.com") -> "$url-w$width-h${width * height / this.width}"
                        url.startsWith("https://yt3.ggpht.com") -> "$url-s$width"
                        else -> url
                    }
                }

                fun size(size: Int): String {
                    return when {
                        url.startsWith("https://lh3.googleusercontent.com") -> "$url-w$size-h$size"
                        url.startsWith("https://yt3.ggpht.com") -> "$url-s$size"
                        else -> url
                    }
                }
            }
        }
    }
}
