package it.vfsfitvnm.youtubemusic

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import it.vfsfitvnm.youtubemusic.models.BrowseResponse
import it.vfsfitvnm.youtubemusic.models.ContinuationResponse
import it.vfsfitvnm.youtubemusic.models.GetQueueResponse
import it.vfsfitvnm.youtubemusic.models.GetSearchSuggestionsResponse
import it.vfsfitvnm.youtubemusic.models.MusicCarouselShelfRenderer
import it.vfsfitvnm.youtubemusic.models.MusicResponsiveListItemRenderer
import it.vfsfitvnm.youtubemusic.models.MusicShelfRenderer
import it.vfsfitvnm.youtubemusic.models.MusicTwoRowItemRenderer
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import it.vfsfitvnm.youtubemusic.models.NextResponse
import it.vfsfitvnm.youtubemusic.models.PlayerResponse
import it.vfsfitvnm.youtubemusic.models.Runs
import it.vfsfitvnm.youtubemusic.models.SearchResponse
import it.vfsfitvnm.youtubemusic.models.SectionListRenderer
import it.vfsfitvnm.youtubemusic.models.ThumbnailRenderer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
object YouTube {
    private const val Key = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

    private val client = HttpClient(OkHttp) {
        BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            brotli()
        }

        defaultRequest {
            url("https://music.youtube.com")
        }
    }

    @Serializable
    data class EmptyBody(
        val context: Context,
    )

    @Serializable
    data class BrowseBody(
        val context: Context,
        val browseId: String,
        val params: String? = null,
    )

    @Serializable
    data class SearchBody(
        val context: Context,
        val query: String,
        val params: String
    )

    @Serializable
    data class PlayerBody(
        val context: Context,
        val videoId: String,
        val playlistId: String?
    )

    @Serializable
    data class GetQueueBody(
        val context: Context,
        val videoIds: List<String>?,
        val playlistId: String?,
    )

    @Serializable
    data class NextBody(
        val context: Context,
        val isAudioOnly: Boolean,
        val videoId: String?,
        val playlistId: String?,
        val tunerSettingValue: String,
        val index: Int?,
        val params: String?,
        val playlistSetVideoId: String?,
        val continuation: String?,
        val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs
    ) {
        @Serializable
        data class WatchEndpointMusicSupportedConfigs(
            val musicVideoType: String
        )
    }

    @Serializable
    data class GetSearchSuggestionsBody(
        val context: Context,
        val input: String
    )

    @Serializable
    data class Context(
        val client: Client,
        val thirdParty: ThirdParty? = null,
    ) {
        @Serializable
        data class Client(
            val clientName: String,
            val clientVersion: String,
            val visitorData: String?,
//            val gl: String = "US",
            val hl: String = "en",
        )

        @Serializable
        data class ThirdParty(
            val embedUrl: String,
        )

        companion object {
            val DefaultWeb = Context(
                client = Client(
                    clientName = "WEB_REMIX",
                    clientVersion = "1.20220328.01.00",
                    visitorData = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
                )
            )

            val DefaultAndroid = Context(
                client = Client(
                    clientName = "ANDROID",
                    clientVersion = "16.50",
                    visitorData = null,
                )
            )

            val DefaultAgeRestrictionBypass = Context(
                client = Client(
                    clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                    clientVersion = "2.0",
                    visitorData = null,
                )
            )
        }
    }

    data class Info<T : NavigationEndpoint.Endpoint>(
        val name: String?,
        val endpoint: T?
    ) {
        @Suppress("UNCHECKED_CAST")
        constructor(run: Runs.Run) : this(
            name = run.text,
            endpoint = run.navigationEndpoint?.endpoint as T?
        )
    }

    sealed class Item {
        abstract val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        abstract val key: String

        data class Song(
            val info: Info<NavigationEndpoint.Endpoint.Watch>?,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
            val album: Info<NavigationEndpoint.Endpoint.Browse>?,
            val durationText: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            override val key: String
                get() = info!!.endpoint!!.videoId!!

            companion object {
                val Filter = Filter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")

                fun from(content: MusicShelfRenderer.Content): Song? {
                    val (mainRuns, otherRuns) = content.runs

                    // Possible configurations:
                    // "song" • author(s) • album • duration
                    // "song" • author(s) • duration
                    // author(s) • album • duration
                    // author(s) • duration

                    val album: Info<NavigationEndpoint.Endpoint.Browse>? = otherRuns
                        .getOrNull(otherRuns.lastIndex - 1)
                        ?.firstOrNull()
                        ?.takeIf { run ->
                            run
                                .navigationEndpoint
                                ?.browseEndpoint
                                ?.type == "MUSIC_PAGE_TYPE_ALBUM"
                        }
                        ?.let(::Info)

                    return Song(
                        info = mainRuns
                            .firstOrNull()
                            ?.let(::Info),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - if (album == null) 1 else 2)
                            ?.map(::Info),
                        album = album,
                        durationText = otherRuns
                            .lastOrNull()
                            ?.firstOrNull()?.text,
                        thumbnail = content
                            .thumbnail
                    ).takeIf { it.info?.endpoint?.videoId != null }
                }

                fun from(renderer: MusicResponsiveListItemRenderer): Song? {
                    return Song(
                        info = renderer
                            .flexColumns
                            .getOrNull(0)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.getOrNull(0)
                            ?.let(::Info),
                        authors = renderer
                            .flexColumns
                            .getOrNull(1)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.map<Runs.Run, Info<NavigationEndpoint.Endpoint.Browse>>(::Info)
                            ?.takeIf(List<Any>::isNotEmpty),
                        durationText = renderer
                            .fixedColumns
                            ?.getOrNull(0)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.getOrNull(0)
                            ?.text,
                        album = renderer
                            .flexColumns
                            .getOrNull(2)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.firstOrNull()
                            ?.let(::Info),
                        thumbnail = renderer
                            .thumbnail
                            ?.musicThumbnailRenderer
                            ?.thumbnail
                            ?.thumbnails
                            ?.firstOrNull()
                    ).takeIf { it.info?.endpoint?.videoId != null }
                }
            }
        }

        data class Video(
            val info: Info<NavigationEndpoint.Endpoint.Watch>?,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
            val viewsText: String?,
            val durationText: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            override val key: String
                get() = info!!.endpoint!!.videoId!!

            val isOfficialMusicVideo: Boolean
                get() = info
                    ?.endpoint
                    ?.watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType == "MUSIC_VIDEO_TYPE_OMV"

            val isUserGeneratedContent: Boolean
                get() = info
                    ?.endpoint
                    ?.watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType == "MUSIC_VIDEO_TYPE_UGC"

            companion object {
                val Filter = Filter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")

                fun from(content: MusicShelfRenderer.Content): Video? {
                    val (mainRuns, otherRuns) = content.runs

                    return Video(
                        info = mainRuns
                            .firstOrNull()
                            ?.let(::Info),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - 2)
                            ?.map(::Info),
                        viewsText = otherRuns
                            .getOrNull(otherRuns.lastIndex - 1)
                            ?.firstOrNull()
                            ?.text,
                        durationText = otherRuns
                            .getOrNull(otherRuns.lastIndex)
                            ?.firstOrNull()
                            ?.text,
                        thumbnail = content
                            .thumbnail
                    ).takeIf { it.info?.endpoint?.videoId != null }
                }
            }
        }

        data class Album(
            val info: Info<NavigationEndpoint.Endpoint.Browse>?,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
            val year: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            override val key: String
                get() = info!!.endpoint!!.browseId!!

            companion object {
                val Filter = Filter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")

                fun from(content: MusicShelfRenderer.Content): Album? {
                    val (mainRuns, otherRuns) = content.runs

                    return Album(
                        info = Info(
                            name = mainRuns
                                .firstOrNull()
                                ?.text,
                            endpoint = content
                                .musicResponsiveListItemRenderer
                                .navigationEndpoint
                                ?.browseEndpoint
                        ),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - 1)
                            ?.map(::Info),
                        year = otherRuns
                            .getOrNull(otherRuns.lastIndex)
                            ?.firstOrNull()
                            ?.text,
                        thumbnail = content
                            .thumbnail
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }

                fun from(renderer: MusicTwoRowItemRenderer): Album? {
                    return Album(
                        info = renderer
                            .title
                            .runs
                            .firstOrNull()
                            ?.let(::Info),
                        authors = null,
                        year = renderer
                            .subtitle
                            .runs
                            .lastOrNull()
                            ?.text,
                        thumbnail = renderer
                            .thumbnailRenderer
                            .musicThumbnailRenderer
                            .thumbnail
                            .thumbnails
                            .firstOrNull()
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }
            }
        }

        data class Artist(
            val info: Info<NavigationEndpoint.Endpoint.Browse>?,
            val subscribersCountText: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            override val key: String
                get() = info!!.endpoint!!.browseId!!

            companion object {
                val Filter = Filter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")

                fun from(content: MusicShelfRenderer.Content): Artist? {
                    val (mainRuns, otherRuns) = content.runs

                    return Artist(
                        info = Info(
                            name = mainRuns
                                .firstOrNull()
                                ?.text,
                            endpoint = content
                                .musicResponsiveListItemRenderer
                                .navigationEndpoint
                                ?.browseEndpoint
                        ),
                        subscribersCountText = otherRuns
                            .lastOrNull()
                            ?.last()
                            ?.text,
                        thumbnail = content
                            .thumbnail
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }

                fun from(renderer: MusicTwoRowItemRenderer): Artist? {
                    return Artist(
                        info = renderer
                            .title
                            .runs
                            .firstOrNull()
                            ?.let(::Info),
                        subscribersCountText = renderer
                            .subtitle
                            .runs
                            .firstOrNull()
                            ?.text,
                        thumbnail = renderer
                            .thumbnailRenderer
                            .musicThumbnailRenderer
                            .thumbnail
                            .thumbnails
                            .firstOrNull()
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }
            }
        }

        data class Playlist(
            val info: Info<NavigationEndpoint.Endpoint.Browse>?,
            val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
            val songCount: Int?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            override val key: String
                get() = info!!.endpoint!!.browseId!!

            companion object {
                fun from(content: MusicShelfRenderer.Content): Playlist? {
                    val (mainRuns, otherRuns) = content.runs

                    return Playlist(
                        info = Info(
                            name = mainRuns
                                .firstOrNull()
                                ?.text ?: "?",
                            endpoint = content
                                .musicResponsiveListItemRenderer
                                .navigationEndpoint
                                ?.browseEndpoint
                        ),
                        channel = otherRuns
                            .firstOrNull()
                            ?.firstOrNull()
                            ?.let(::Info),
                        songCount = otherRuns
                            .lastOrNull()
                            ?.firstOrNull()
                            ?.text
                            ?.split(' ')
                            ?.firstOrNull()
                            ?.toIntOrNull(),
                        thumbnail = content
                            .thumbnail
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }

                fun from(renderer: MusicTwoRowItemRenderer): Playlist? {
                    return Playlist(
                        info = renderer
                            .title
                            .runs
                            .firstOrNull()
                            ?.let(::Info),
                        channel = renderer
                            .subtitle
                            .runs
                            .getOrNull(2)
                            ?.let(::Info),
                        songCount = renderer
                            .subtitle
                            .runs
                            .getOrNull(4)
                            ?.text
                            ?.split(' ')
                            ?.firstOrNull()
                            ?.toIntOrNull(),
                        thumbnail = renderer
                            .thumbnailRenderer
                            .musicThumbnailRenderer
                            .thumbnail
                            .thumbnails
                            .firstOrNull()
                    ).takeIf { it.info?.endpoint?.browseId != null }
                }
            }
        }

        object CommunityPlaylist {
            val Filter = Filter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
        }

        object FeaturedPlaylist {
            val Filter = Filter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
        }

        @JvmInline
        value class Filter(val value: String)
    }

    class SearchResult(val items: List<Item>?, val continuation: String?)

    suspend fun search(
        query: String,
        filter: String,
        continuation: String?
    ): Result<SearchResult>? {
        return runCatching {
            val musicShelfRenderer = client.post("/youtubei/v1/search") {
                contentType(ContentType.Application.Json)
                setBody(
                    SearchBody(
                        context = Context.DefaultWeb,
                        query = query,
                        params = filter
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
                parameter("continuation", continuation)
            }.let { response ->
                if (continuation == null) {
                    response.body<SearchResponse>()
                        .contents
                        .tabbedSearchResultsRenderer
                        .tabs
                        .firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.lastOrNull()
                        ?.musicShelfRenderer
                } else {
                    response.body<ContinuationResponse>()
                        .continuationContents
                        ?.musicShelfContinuation
                }
            }
            SearchResult(
                items = musicShelfRenderer
                    ?.contents
                    ?.mapNotNull(
                        when (filter) {
                            Item.Song.Filter.value -> Item.Song.Companion::from
                            Item.Album.Filter.value -> Item.Album.Companion::from
                            Item.Artist.Filter.value -> Item.Artist.Companion::from
                            Item.Video.Filter.value -> Item.Video.Companion::from
                            Item.CommunityPlaylist.Filter.value -> Item.Playlist.Companion::from
                            Item.FeaturedPlaylist.Filter.value -> Item.Playlist.Companion::from
                            else -> error("Unknown filter: $filter")
                        }
                    ),
                continuation = musicShelfRenderer
                    ?.continuations
                    ?.firstOrNull()
                    ?.nextContinuationData
                    ?.continuation
            )
        }.recoverIfCancelled()
    }

    suspend fun getSearchSuggestions(input: String): Result<List<String>?>? {
        return runCatching {
            val body = client.post("/youtubei/v1/music/get_search_suggestions") {
                contentType(ContentType.Application.Json)
                setBody(
                    GetSearchSuggestionsBody(
                        context = Context.DefaultWeb,
                        input = input
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<GetSearchSuggestionsResponse>()

            body
                .contents
                ?.flatMap { content ->
                    content
                        .searchSuggestionsSectionRenderer
                        .contents.mapNotNull {
                            it
                                .searchSuggestionRenderer
                                .navigationEndpoint
                                .searchEndpoint
                                ?.query
                        }
                }
        }.recoverIfCancelled()
    }

    suspend fun player(videoId: String, playlistId: String? = null): Result<PlayerResponse>? {
        return runCatching {
            val playerResponse = client.post("/youtubei/v1/player") {
                contentType(ContentType.Application.Json)
                setBody(
                    PlayerBody(
                        context = Context.DefaultAndroid,
                        videoId = videoId,
                        playlistId = playlistId,
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<PlayerResponse>()

            if (playerResponse.playabilityStatus.status == "OK") {
                playerResponse
            } else {
                @Serializable
                data class AudioStream(
                    val url: String,
                    val bitrate: Long
                )

                @Serializable
                data class PipedResponse(
                    val audioStreams: List<AudioStream>
                )

                val safePlayerResponse = client.post("/youtubei/v1/player") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        PlayerBody(
                            context = Context.DefaultAgeRestrictionBypass,
                            videoId = videoId,
                            playlistId = playlistId,
                        )
                    )
                    parameter("key", Key)
                    parameter("prettyPrint", false)
                }.body<PlayerResponse>()

                if (safePlayerResponse.playabilityStatus.status != "OK") {
                    return@runCatching playerResponse
                }

                val audioStreams = client.get("https://watchapi.whatever.social/streams/$videoId") {
                    contentType(ContentType.Application.Json)
                }.body<PipedResponse>().audioStreams

                safePlayerResponse.copy(
                    streamingData = safePlayerResponse.streamingData?.copy(
                        adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats.map { adaptiveFormat ->
                            adaptiveFormat.copy(
                                url = audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.url
                            )
                        }
                    )
                )
            }
        }.recoverIfCancelled()
    }

    private suspend fun getQueue(body: GetQueueBody): Result<List<Item.Song>?>? {
        return runCatching {
            val response = client.post("/youtubei/v1/music/get_queue") {
                contentType(ContentType.Application.Json)
                setBody(body)
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<GetQueueResponse>()

            response.queueDatas?.mapNotNull { queueData ->
                queueData.content?.playlistPanelVideoRenderer?.let { renderer ->
                    Item.Song(
                        info = Info(
                            name = renderer
                                .title
                                ?.text,
                            endpoint = renderer
                                .navigationEndpoint
                                .watchEndpoint
                        ),
                        authors = renderer
                            .longBylineText
                            ?.splitBySeparator()
                            ?.getOrNull(0)
                            ?.map(::Info),
                        album = renderer
                            .longBylineText
                            ?.splitBySeparator()
                            ?.getOrNull(1)
                            ?.getOrNull(0)
                            ?.let(::Info),
                        thumbnail = renderer
                            .thumbnail
                            .thumbnails
                            .getOrNull(0),
                        durationText = renderer
                            .lengthText
                            ?.text
                    ).takeIf { it.info?.endpoint?.videoId != null }
                }
            }
        }.recoverIfCancelled()
    }

    suspend fun song(videoId: String): Result<Item.Song?>? {
        return getQueue(
            GetQueueBody(
                context = Context.DefaultWeb,
                videoIds = listOf(videoId),
                playlistId = null
            )
        )?.map { it?.firstOrNull() }
    }

    suspend fun next(
        videoId: String?,
        playlistId: String?,
        index: Int? = null,
        params: String? = null,
        playlistSetVideoId: String? = null,
        continuation: String? = null,
    ): Result<NextResult>? {
        return runCatching {
            val body = client.post("/youtubei/v1/next") {
                contentType(ContentType.Application.Json)
                setBody(
                    NextBody(
                        context = Context.DefaultWeb,
                        videoId = videoId,
                        playlistId = playlistId,
                        isAudioOnly = true,
                        tunerSettingValue = "AUTOMIX_SETTING_NORMAL",
                        watchEndpointMusicSupportedConfigs = NextBody.WatchEndpointMusicSupportedConfigs(
                            musicVideoType = "MUSIC_VIDEO_TYPE_ATV"
                        ),
                        index = index,
                        playlistSetVideoId = playlistSetVideoId,
                        params = params,
                        continuation = continuation
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<NextResponse>()

            val tabs = body
                .contents
                .singleColumnMusicWatchNextResultsRenderer
                .tabbedRenderer
                .watchNextTabbedResultsRenderer
                .tabs

            NextResult(
                playlistId = playlistId,
                playlistSetVideoId = playlistSetVideoId,
                params = params,
                continuation = (tabs
                    .getOrNull(0)
                    ?.tabRenderer
                    ?.content
                    ?.musicQueueRenderer
                    ?.content
                    ?: body.continuationContents)
                    ?.playlistPanelRenderer
                    ?.continuations
                    ?.getOrNull(0)
                    ?.nextContinuationData
                    ?.continuation,
                items = (tabs
                    .getOrNull(0)
                    ?.tabRenderer
                    ?.content
                    ?.musicQueueRenderer
                    ?.content
                    ?: body.continuationContents)
                    ?.playlistPanelRenderer
                    ?.contents
                    ?.also {
                        // TODO: we should parse the MusicResponsiveListItemRenderer menu so we can
                        //  avoid an extra network request
                        it.lastOrNull()
                            ?.automixPreviewVideoRenderer
                            ?.content
                            ?.automixPlaylistVideoRenderer
                            ?.navigationEndpoint
                            ?.watchPlaylistEndpoint
                            ?.let { endpoint ->
                                return next(
                                    videoId = videoId,
                                    playlistId = endpoint.playlistId,
                                    params = endpoint.params
                                )
                            }
                    }
                    ?.mapNotNull { it.playlistPanelVideoRenderer }
                    ?.mapNotNull { renderer ->
                        Item.Song(
                            info = Info(
                                name = renderer
                                    .title
                                    ?.text,
                                endpoint = renderer
                                    .navigationEndpoint
                                    .watchEndpoint
                            ),
                            authors = renderer
                                .longBylineText
                                ?.splitBySeparator()
                                ?.getOrNull(0)
                                ?.map(::Info),
                            album = renderer
                                .longBylineText
                                ?.splitBySeparator()
                                ?.getOrNull(1)
                                ?.getOrNull(0)
                                ?.let(::Info),
                            thumbnail = renderer
                                .thumbnail
                                .thumbnails
                                .firstOrNull(),
                            durationText = renderer
                                .lengthText
                                ?.text
                        ).takeIf { it.info?.endpoint?.videoId != null }
                    },
                lyricsBrowseId = tabs
                    .getOrNull(1)
                    ?.tabRenderer
                    ?.endpoint
                    ?.browseEndpoint
                    ?.browseId,
            )
        }.recoverIfCancelled()
    }

    data class NextResult(
        val continuation: String?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null,
        val items: List<Item.Song>?,
        val lyricsBrowseId: String?
    ) {
        suspend fun lyrics(): Result<String?>? {
            return if (lyricsBrowseId == null) {
                Result.success(null)
            } else {
                browse(lyricsBrowseId)?.map { body ->
                    body.contents
                        ?.sectionListRenderer
                        ?.contents
                        ?.firstOrNull()
                        ?.musicDescriptionShelfRenderer
                        ?.description
                        ?.text
                }
            }
        }
    }

    suspend fun browse(browseId: String): Result<BrowseResponse>? {
        return runCatching {
            client.post("/youtubei/v1/browse") {
                contentType(ContentType.Application.Json)
                setBody(
                    BrowseBody(
                        browseId = browseId,
                        context = Context.DefaultWeb
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<BrowseResponse>()
        }.recoverIfCancelled()
    }

    data class ItemsResult<T : Item>(
        val items: List<T>?,
        val continuation: String?
    )

    suspend fun <T : Item> items(
        browseId: String,
        continuation: String?,
        block: (MusicResponsiveListItemRenderer) -> T?
    ): Result<ItemsResult<T>?>? {
        return runCatching {
            val response = client.post("/youtubei/v1/browse") {
                contentType(ContentType.Application.Json)
                setBody(
                    BrowseBody(
                        browseId = browseId,
                        context = Context.DefaultWeb
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
                parameter("continuation", continuation)
            }

            if (continuation == null) {
                response
                    .body<BrowseResponse>()
                    .contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.firstOrNull()
                    ?.musicShelfRenderer
            } else {
                response
                    .body<ContinuationResponse>()
                    .continuationContents
                    ?.musicShelfContinuation
            }?.let { musicShelfRenderer ->
                ItemsResult(
                    items = musicShelfRenderer
                        .contents
                        .mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                        .mapNotNull(block),
                    continuation = musicShelfRenderer
                        .continuations
                        ?.firstOrNull()
                        ?.nextContinuationData
                        ?.continuation
                )
            }
        }.recoverIfCancelled()
    }

    suspend fun <T : Item> items2(
        browseId: String,
        params: String?,
        block: (MusicTwoRowItemRenderer) -> T?
    ): Result<ItemsResult<T>?>? {
        return runCatching {
            client.post("/youtubei/v1/browse") {
                contentType(ContentType.Application.Json)
                setBody(
                    BrowseBody(
                        browseId = browseId,
                        context = Context.DefaultWeb,
                        params = params
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }
                .body<BrowseResponse>()
                .contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.gridRenderer
                ?.let { gridRenderer ->
                    ItemsResult(
                        items = gridRenderer
                            .items
                            ?.mapNotNull(SectionListRenderer.Content.GridRenderer.Item::musicTwoRowItemRenderer)
                            ?.mapNotNull(block),
                        continuation = null
                    )
                }
        }.recoverIfCancelled()
    }

    data class PlaylistOrAlbum(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?,
        val songs: List<Item.Song>?,
        val url: String?,
        val continuation: String?,
    ) {
        suspend fun next(): PlaylistOrAlbum {
            return continuation?.let {
                runCatching {
                    client.post("/youtubei/v1/browse") {
                        contentType(ContentType.Application.Json)
                        setBody(EmptyBody(context = Context.DefaultWeb))
                        parameter("key", Key)
                        parameter("prettyPrint", false)
                        parameter("continuation", continuation)
                    }.body<ContinuationResponse>().let { continuationResponse ->
                        copy(
                            songs = songs?.plus(
                                continuationResponse
                                    .continuationContents
                                    ?.musicShelfContinuation
                                    ?.contents
                                    ?.map(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                                    ?.mapNotNull(Item.Song.Companion::from) ?: emptyList()
                            ),
                            continuation = continuationResponse
                                .continuationContents
                                ?.musicShelfContinuation
                                ?.continuations
                                ?.firstOrNull()
                                ?.nextContinuationData
                                ?.continuation
                        ).next()
                    }
                }.recoverIfCancelled()?.getOrNull()
            } ?: this
        }
    }

    suspend fun album(browseId: String): Result<PlaylistOrAlbum>? {
        return playlistOrAlbum(browseId)?.map { album ->
            album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
                playlistOrAlbum("VL$playlistId")?.getOrNull()?.let { playlist ->
                    album.copy(songs = playlist.songs)
                }
            } ?: album
        }?.map { album ->
            val albumInfo = Info(
                name = album.title ?: "",
                endpoint = NavigationEndpoint.Endpoint.Browse(
                    browseId = browseId,
                    params = null,
                    browseEndpointContextSupportedConfigs = null
                )
            )

            album.copy(
                songs = album.songs?.map { song ->
                    song.copy(
                        authors = song.authors ?: album.authors,
                        album = albumInfo,
                        thumbnail = album.thumbnail
                    )
                }
            )
        }
    }

    suspend fun playlist(browseId: String): Result<PlaylistOrAlbum>? {
        return playlistOrAlbum(browseId)
    }

    private suspend fun playlistOrAlbum(browseId: String): Result<PlaylistOrAlbum>? {
        return browse(browseId)?.map { body ->
            PlaylistOrAlbum(
                title = body
                    .header
                    ?.musicDetailHeaderRenderer
                    ?.title
                    ?.text,
                thumbnail = body
                    .header
                    ?.musicDetailHeaderRenderer
                    ?.thumbnail
                    ?.musicThumbnailRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.firstOrNull(),
                authors = body
                    .header
                    ?.musicDetailHeaderRenderer
                    ?.subtitle
                    ?.splitBySeparator()
                    ?.getOrNull(1)
                    ?.map(::Info),
                year = body
                    .header
                    ?.musicDetailHeaderRenderer
                    ?.subtitle
                    ?.splitBySeparator()
                    ?.getOrNull(2)
                    ?.firstOrNull()
                    ?.text,
                songs = body
                    .contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.firstOrNull()
                    ?.musicShelfRenderer
                    ?.contents
                    ?.map(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                    ?.mapNotNull(Item.Song.Companion::from),
                url = body
                    .microformat
                    ?.microformatDataRenderer
                    ?.urlCanonical,
                continuation = body
                    .contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.firstOrNull()
                    ?.musicShelfRenderer
                    ?.continuations
                    ?.firstOrNull()
                    ?.nextContinuationData
                    ?.continuation
            )
        }
    }

    data class Artist(
        val name: String?,
        val description: String?,
        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val songs: List<Item.Song>?,
        val songsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val albums: List<Item.Album>?,
        val albumsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val singles: List<Item.Album>?,
        val singlesEndpoint: NavigationEndpoint.Endpoint.Browse?,
    )

    suspend fun artist(browseId: String): Result<Artist>? {
        return browse(browseId)?.map { response ->
            fun findSectionByTitle(text: String): SectionListRenderer.Content? {
                return response
                    .contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.get(0)
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.find { content ->
                        val title = content
                            .musicCarouselShelfRenderer
                            ?.header
                            ?.musicCarouselShelfBasicHeaderRenderer
                            ?.title
                            ?: content
                                .musicShelfRenderer
                                ?.title

                        title
                            ?.runs
                            ?.firstOrNull()
                            ?.text == text
                    }
            }

            val songsSection = findSectionByTitle("Songs")?.musicShelfRenderer
            val albumsSection = findSectionByTitle("Albums")?.musicCarouselShelfRenderer
            val singlesSection = findSectionByTitle("Singles")?.musicCarouselShelfRenderer

            Artist(
                name = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.title
                    ?.text,
                description = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.description
                    ?.text
                    ?.substringBeforeLast("\n\nFrom Wikipedia"),
                thumbnail = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.thumbnail
                    ?.musicThumbnailRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.getOrNull(0),
                shuffleEndpoint = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.playButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint,
                radioEndpoint = response
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.startRadioButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint,
                songs = songsSection
                    ?.contents
                    ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                    ?.mapNotNull(Item.Song::from),
                songsEndpoint = songsSection
                    ?.bottomEndpoint
                    ?.browseEndpoint,
                albums = albumsSection
                    ?.contents
                    ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                    ?.mapNotNull(Item.Album::from),
                albumsEndpoint = albumsSection
                    ?.header
                    ?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.browseEndpoint,
                singles = singlesSection
                    ?.contents
                    ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                    ?.mapNotNull(Item.Album::from),
                singlesEndpoint = singlesSection
                    ?.header
                    ?.musicCarouselShelfBasicHeaderRenderer
                    ?.moreContentButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.browseEndpoint,
            )
        }
    }

    data class Related(
        val songs: List<Item.Song>? = null,
        val playlists: List<Item.Playlist>? = null,
        val albums: List<Item.Album>? = null,
        val artists: List<Item.Artist>? = null,
    )

    suspend fun related(videoId: String): Result<Related?>? {
        return runCatching {
            val body = client.post("/youtubei/v1/next") {
                contentType(ContentType.Application.Json)
                setBody(
                    NextBody(
                        context = Context.DefaultWeb,
                        videoId = videoId,
                        playlistId = null,
                        isAudioOnly = true,
                        tunerSettingValue = "AUTOMIX_SETTING_NORMAL",
                        watchEndpointMusicSupportedConfigs = NextBody.WatchEndpointMusicSupportedConfigs(
                            musicVideoType = "MUSIC_VIDEO_TYPE_ATV"
                        ),
                        index = 0,
                        playlistSetVideoId = null,
                        params = null,
                        continuation = null
                    )
                )
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<NextResponse>()

            body
                .contents
                .singleColumnMusicWatchNextResultsRenderer
                .tabbedRenderer
                .watchNextTabbedResultsRenderer
                .tabs
                .getOrNull(2)
                ?.tabRenderer
                ?.endpoint
                ?.browseEndpoint
                ?.browseId
                ?.let { browseId ->
                    browse(browseId)?.getOrThrow()?.let { browseResponse ->
                        browseResponse
                            .contents
                            ?.sectionListRenderer
                            ?.contents
                            ?.mapNotNull(SectionListRenderer.Content::musicCarouselShelfRenderer)
                            ?.map(MusicCarouselShelfRenderer::contents)
                    }
                }?.let { contents ->
                    Related(
                        songs = contents.find { items ->
                            items.firstOrNull()?.musicResponsiveListItemRenderer != null
                        }?.mapNotNull { content ->
                            Item.Song.from(content.musicResponsiveListItemRenderer!!)
                        },
                        playlists = contents.find { items ->
                            items.firstOrNull()
                                ?.musicTwoRowItemRenderer
                                ?.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseEndpointContextSupportedConfigs
                                ?.browseEndpointContextMusicConfig
                                ?.pageType == "MUSIC_PAGE_TYPE_PLAYLIST"
                        }
                            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                            ?.mapNotNull(Item.Playlist.Companion::from),
                        albums = contents.find { items ->
                            items.firstOrNull()
                                ?.musicTwoRowItemRenderer
                                ?.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseEndpointContextSupportedConfigs
                                ?.browseEndpointContextMusicConfig
                                ?.pageType == "MUSIC_PAGE_TYPE_ALBUM"
                        }
                            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                            ?.mapNotNull(Item.Album.Companion::from),
                        artists = contents.find { items ->
                            items.firstOrNull()
                                ?.musicTwoRowItemRenderer
                                ?.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseEndpointContextSupportedConfigs
                                ?.browseEndpointContextMusicConfig
                                ?.pageType == "MUSIC_PAGE_TYPE_ARTIST"
                        }
                            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                            ?.mapNotNull(Item.Artist.Companion::from),
                    )
                }
        }.recoverIfCancelled()
    }
}
