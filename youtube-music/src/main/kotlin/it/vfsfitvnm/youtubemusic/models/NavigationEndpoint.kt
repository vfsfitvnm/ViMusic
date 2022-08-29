package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable

/**
 * watchPlaylistEndpoint: params, playlistId
 * watchEndpoint: params, playlistId, videoId, index
 * browseEndpoint: params, browseId
 * searchEndpoint: params, query
 */
//@Serializable
//data class NavigationEndpoint(
//    @JsonNames("watchEndpoint", "watchPlaylistEndpoint", "navigationEndpoint", "browseEndpoint", "searchEndpoint")
//    val endpoint: Endpoint
//) {
//    @Serializable
//    data class Endpoint(
//        val params: String?,
//        val playlistId: String?,
//        val videoId: String?,
//        val index: Int?,
//        val browseId: String?,
//        val query: String?,
//        val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs?,
//        val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
//    ) {
//        @Serializable
//        data class WatchEndpointMusicSupportedConfigs(
//            val watchEndpointMusicConfig: WatchEndpointMusicConfig
//        ) {
//            @Serializable
//            data class WatchEndpointMusicConfig(
//                val musicVideoType: String
//            )
//        }
//
//        @Serializable
//        data class BrowseEndpointContextSupportedConfigs(
//            val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
//        ) {
//            @Serializable
//            data class BrowseEndpointContextMusicConfig(
//                val pageType: String
//            )
//        }
//    }
//}

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: Endpoint.Watch?,
    val watchPlaylistEndpoint: Endpoint.WatchPlaylist?,
    val browseEndpoint: Endpoint.Browse?,
    val searchEndpoint: Endpoint.Search?,
) {
    val endpoint: Endpoint?
        get() = watchEndpoint ?: browseEndpoint ?: watchPlaylistEndpoint ?: searchEndpoint

    @Serializable
    sealed class Endpoint {
        @Serializable
        data class Watch(
            val params: String? = null,
            val playlistId: String? = null,
            val videoId: String? = null,
            val index: Int? = null,
            val playlistSetVideoId: String? = null,
            val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null,
        ) : Endpoint() {
            val type: String?
                get() = watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType

            @Serializable
            data class WatchEndpointMusicSupportedConfigs(
                val watchEndpointMusicConfig: WatchEndpointMusicConfig
            ) {

                @Serializable
                data class WatchEndpointMusicConfig(
                    val musicVideoType: String
                )
            }
        }

        @Serializable
        data class WatchPlaylist(
            val params: String?,
            val playlistId: String,
        ) : Endpoint()

        @Serializable
        data class Browse(
            val params: String?,
            val browseId: String,
            val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
        ) : Endpoint() {
            val type: String?
                get() = browseEndpointContextSupportedConfigs
                    ?.browseEndpointContextMusicConfig
                    ?.pageType

            @Serializable
            data class BrowseEndpointContextSupportedConfigs(
                val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
            ) {

                @Serializable
                data class BrowseEndpointContextMusicConfig(
                    val pageType: String
                )
            }
        }

        @Serializable
        data class Search(
            val params: String?,
            val query: String,
        ) : Endpoint()
    }
}

//@Serializable(with = NavigationEndpoint.Serializer::class)
//sealed class NavigationEndpoint {
//    @Serializable
//    data class Watch(
//        val watchEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val playlistId: String,
//            val videoId: String,
////            val index: Int?
//            val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs,
//        )
//
//        @Serializable
//        data class WatchEndpointMusicSupportedConfigs(
//            val watchEndpointMusicConfig: WatchEndpointMusicConfig
//        ) {
//            @Serializable
//            data class WatchEndpointMusicConfig(
//                val musicVideoType: String
//            )
//        }
//    }
//
//    @Serializable
//    data class WatchPlaylist(
//        val watchPlaylistEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val playlistId: String,
//        )
//    }
//
//    @Serializable
//    data class Browse(
//        val browseEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val browseId: String,
//            val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs,
//        )
//
//        @Serializable
//        data class BrowseEndpointContextSupportedConfigs(
//            val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
//        ) {
//            @Serializable
//            data class BrowseEndpointContextMusicConfig(
//                val pageType: String
//            )
//        }
//    }
//
//    @Serializable
//    data class Search(
//        val searchEndpoint: Data
//    ) : NavigationEndpoint() {
//        @Serializable
//        data class Data(
//            val params: String?,
//            val query: String,
//        )
//    }
//
//    object Serializer : JsonContentPolymorphicSerializer<NavigationEndpoint>(NavigationEndpoint::class) {
//        override fun selectDeserializer(element: JsonElement) = when {
//            "watchEndpoint" in element.jsonObject -> Watch.serializer()
//            "watchPlaylistEndpoint" in element.jsonObject -> WatchPlaylist.serializer()
//            "browseEndpoint" in element.jsonObject -> Browse.serializer()
//            "searchEndpoint" in element.jsonObject -> Search.serializer()
//            else -> TODO()
//        }
//    }
//}
