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
import it.vfsfitvnm.youtubemusic.models.MusicResponsiveListItemRenderer
import it.vfsfitvnm.youtubemusic.models.MusicShelfRenderer
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import it.vfsfitvnm.youtubemusic.models.NextResponse
import it.vfsfitvnm.youtubemusic.models.PlayerResponse
import it.vfsfitvnm.youtubemusic.models.Runs
import it.vfsfitvnm.youtubemusic.models.SearchResponse
import it.vfsfitvnm.youtubemusic.models.ThumbnailRenderer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object YouTube {
    private const val Key = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

    @OptIn(ExperimentalSerializationApi::class)
    val client = HttpClient(OkHttp) {
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
        val name: String,
        val endpoint: T?
    ) {
        companion object {
            inline fun <reified T : NavigationEndpoint.Endpoint> from(run: Runs.Run): Info<T> {
                return Info(
                    name = run.text,
                    endpoint = run.navigationEndpoint?.endpoint as T?
                )
            }
        }
    }

    sealed class Item {
        abstract val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?

        data class Song(
            val info: Info<NavigationEndpoint.Endpoint.Watch>,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>,
            val album: Info<NavigationEndpoint.Endpoint.Browse>?,
            val durationText: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            companion object : FromMusicShelfRendererContent<Song> {
                val Filter = Filter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")

                override fun from(content: MusicShelfRenderer.Content): Song {
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
                        ?.let(Info.Companion::from)

                    return Song(
                        info = Info.from(mainRuns.first()),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - if (album == null) 1 else 2)
                            ?.map(Info.Companion::from)
                            ?: emptyList(),
                        album = album,
                        durationText = otherRuns
                            .lastOrNull()
                            ?.firstOrNull()?.text,
                        thumbnail = content
                            .thumbnail
                    )
                }
            }
        }

        data class Video(
            val info: Info<NavigationEndpoint.Endpoint.Watch>,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>,
            val views: List<Info<NavigationEndpoint.Endpoint.Browse>>,
            val durationText: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            val isOfficialMusicVideo: Boolean
                get() = info
                    .endpoint
                    ?.watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType == "MUSIC_VIDEO_TYPE_OMV"

            val isUserGeneratedContent: Boolean
                get() = info
                    .endpoint
                    ?.watchEndpointMusicSupportedConfigs
                    ?.watchEndpointMusicConfig
                    ?.musicVideoType == "MUSIC_VIDEO_TYPE_UGC"

            companion object : FromMusicShelfRendererContent<Video> {
                val Filter = Filter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")

                override fun from(content: MusicShelfRenderer.Content): Video {
                    val (mainRuns, otherRuns) = content.runs

                    return Video(
                        info = Info.from(mainRuns.first()),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - 2)
                            ?.map(Info.Companion::from)
                            ?: emptyList(),
                        views = otherRuns
                            .getOrNull(otherRuns.lastIndex - 1)
                            ?.map(Info.Companion::from) ?: emptyList(),
                        durationText = otherRuns
                            .getOrNull(otherRuns.lastIndex)
                            ?.first()
                            ?.text,
                        thumbnail = content
                            .thumbnail
                    )
                }
            }
        }

        data class Album(
            val info: Info<NavigationEndpoint.Endpoint.Browse>,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
            val year: String?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            companion object : FromMusicShelfRendererContent<Album> {
                val Filter = Filter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")

                override fun from(content: MusicShelfRenderer.Content): Album {
                    val (mainRuns, otherRuns) = content.runs

                    return Album(
                        info = Info(
                            name = mainRuns
                                .first()
                                .text,
                            endpoint = content
                                .musicResponsiveListItemRenderer
                                .navigationEndpoint
                                ?.browseEndpoint
                        ),
                        authors = otherRuns
                            .getOrNull(otherRuns.lastIndex - 1)
                            ?.map(Info.Companion::from),
                        year = otherRuns
                            .getOrNull(otherRuns.lastIndex)
                            ?.firstOrNull()
                            ?.text,
                        thumbnail = content
                            .thumbnail
                    )
                }
            }
        }

        data class Artist(
            val info: Info<NavigationEndpoint.Endpoint.Browse>,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            companion object : FromMusicShelfRendererContent<Artist> {
                val Filter = Filter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")

                override fun from(content: MusicShelfRenderer.Content): Artist {
                    val (mainRuns) = content.runs

                    return Artist(
                        info = Info(
                            name = mainRuns
                                .first()
                                .text,
                            endpoint = content
                                .musicResponsiveListItemRenderer
                                .navigationEndpoint
                                ?.browseEndpoint
                        ),
                        thumbnail = content
                            .thumbnail
                    )
                }
            }
        }

        data class Playlist(
            val info: Info<NavigationEndpoint.Endpoint.Browse>,
            val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
            val songCount: Int?,
            override val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?
        ) : Item() {
            companion object : FromMusicShelfRendererContent<Playlist> {
                override fun from(content: MusicShelfRenderer.Content): Playlist {
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
                            ?.let { Info.from(it) },
                        songCount = otherRuns
                            .lastOrNull()
                            ?.firstOrNull()
                            ?.text
                            ?.split(' ')
                            ?.firstOrNull()
                            ?.toIntOrNull(),
                        thumbnail = content
                            .thumbnail
                    )
                }
            }
        }

        object CommunityPlaylist {
            val Filter = Filter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
        }

        object FeaturedPlaylist {
            val Filter = Filter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
        }

        interface FromMusicShelfRendererContent<out T : Item> {
            fun from(content: MusicShelfRenderer.Content): T
        }

        @JvmInline
        value class Filter(val value: String)
    }

    class SearchResult(val items: List<Item>, val continuation: String?)

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
                        .musicShelfContinuation
                }
            }
            SearchResult(
                items = musicShelfRenderer
                    ?.contents
                    ?.map(
                        when (filter) {
                            Item.Song.Filter.value -> Item.Song.Companion::from
                            Item.Album.Filter.value -> Item.Album.Companion::from
                            Item.Artist.Filter.value -> Item.Artist.Companion::from
                            Item.Video.Filter.value -> Item.Video.Companion::from
                            Item.CommunityPlaylist.Filter.value -> Item.Playlist.Companion::from
                            Item.FeaturedPlaylist.Filter.value -> Item.Playlist.Companion::from
                            else -> error("Unknown filter: $filter")
                        }
                    ) ?: emptyList(),
                continuation = musicShelfRenderer
                    ?.continuations
                    ?.firstOrNull()
                    ?.nextRadioContinuationData
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
            val body = client.post("/youtubei/v1/music/get_queue") {
                contentType(ContentType.Application.Json)
                setBody(body)
                parameter("key", Key)
                parameter("prettyPrint", false)
            }.body<GetQueueResponse>()

            body.queueDatas?.mapNotNull { queueData ->
                queueData.content?.playlistPanelVideoRenderer?.let { renderer ->
                    Item.Song(
                        info = Info(
                            name = renderer
                                .title
                                ?.text ?: return@let null,
                            endpoint = renderer
                                .navigationEndpoint
                                .watchEndpoint
                        ),
                        authors = renderer
                            .longBylineText
                            ?.splitBySeparator()
                            ?.getOrNull(0)
                            ?.map { Info.from(it) }
                            ?: emptyList(),
                        album = renderer
                            .longBylineText
                            ?.splitBySeparator()
                            ?.getOrNull(1)
                            ?.getOrNull(0)
                            ?.let { Info.from(it) },
                        thumbnail = renderer
                            .thumbnail
                            .thumbnails
                            .getOrNull(0),
                        durationText = renderer
                            .lengthText
                            ?.text
                    )
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

    suspend fun queue(playlistId: String): Result<List<Item.Song>?>? {
        return getQueue(
            GetQueueBody(
                context = Context.DefaultWeb,
                videoIds = null,
                playlistId = playlistId
            )
        )
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
                    ?.nextRadioContinuationData
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
                                    ?.text ?: return@mapNotNull null,
                                endpoint = renderer
                                    .navigationEndpoint
                                    .watchEndpoint
                            ),
                            authors = renderer
                                .longBylineText
                                ?.splitBySeparator()
                                ?.getOrNull(0)
                                ?.map { run -> Info.from(run) }
                                ?: emptyList(),
                            album = renderer
                                .longBylineText
                                ?.splitBySeparator()
                                ?.getOrNull(1)
                                ?.getOrNull(0)
                                ?.let { run -> Info.from(run) },
                            thumbnail = renderer
                                .thumbnail
                                .thumbnails
                                .firstOrNull(),
                            durationText = renderer
                                .lengthText
                                ?.text
                        )
                    },
                lyrics = NextResult.Lyrics(
                    browseId = tabs
                        .getOrNull(1)
                        ?.tabRenderer
                        ?.endpoint
                        ?.browseEndpoint
                        ?.browseId
                ),
                related = NextResult.Related(
                    browseId = tabs
                        .getOrNull(2)
                        ?.tabRenderer
                        ?.endpoint
                        ?.browseEndpoint
                        ?.browseId
                )
            )
        }.recoverIfCancelled()
    }

    data class NextResult(
        val continuation: String?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null,
        val items: List<Item.Song>?,
        val lyrics: Lyrics?,
        val related: Related?,
    ) {
        class Lyrics(
            val browseId: String?,
        ) {
            suspend fun text(): Result<String?>? {
                return if (browseId == null) {
                    Result.success(null)
                } else {
                    browse(browseId)?.map { body ->
                        body.contents
                            .sectionListRenderer
                            ?.contents
                            ?.first()
                            ?.musicDescriptionShelfRenderer
                            ?.description
                            ?.text
                    }
                }
            }
        }

        class Related(
            val browseId: String?,
        )
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

    data class PlaylistOrAlbum(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?,
        val items: List<Item>?,
        val url: String?,
        val continuation: String?,
    ) {
        data class Item(
            val info: Info<NavigationEndpoint.Endpoint.Watch>,
            val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
            val durationText: String?,
            val album: Info<NavigationEndpoint.Endpoint.Browse>?,
            val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?,
        ) {
            companion object {
                fun from(renderer: MusicResponsiveListItemRenderer): Item? {
                    return Item(
                        info = renderer
                            .flexColumns
                            .getOrNull(0)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.getOrNull(0)
                            ?.let { Info.from(it) } ?: return null,
                        authors = renderer
                            .flexColumns
                            .getOrNull(1)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.map { Info.from<NavigationEndpoint.Endpoint.Browse>(it) }
                            ?.takeIf { it.isNotEmpty() },
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
                            ?.let { Info.from(it) },
                        thumbnail = renderer
                            .thumbnail
                            ?.musicThumbnailRenderer
                            ?.thumbnail
                            ?.thumbnails
                            ?.firstOrNull()
                    )
                }
            }
        }

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
                            items = items?.plus(continuationResponse
                                .continuationContents
                                .musicShelfContinuation
                                ?.contents
                                ?.map(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                                ?.mapNotNull(Item.Companion::from) ?: emptyList()),
                            continuation = continuationResponse
                                .continuationContents
                                .musicShelfContinuation
                                ?.continuations
                                ?.firstOrNull()
                                ?.nextRadioContinuationData
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
                    album.copy(items = playlist.items)
                }
            } ?: album
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
                    ?.map { Info.from(it) },
                year = body
                    .header
                    ?.musicDetailHeaderRenderer
                    ?.subtitle
                    ?.splitBySeparator()
                    ?.getOrNull(2)
                    ?.firstOrNull()
                    ?.text,
                items = body
                    .contents
                    .singleColumnBrowseResultsRenderer
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
                    ?.mapNotNull(PlaylistOrAlbum.Item.Companion::from)
//                    ?.filter { it.info.endpoint != null }
                ,
                url = body
                    .microformat
                    ?.microformatDataRenderer
                    ?.urlCanonical,
                continuation = body
                    .contents
                    .singleColumnBrowseResultsRenderer
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
                    ?.nextRadioContinuationData
                    ?.continuation
            )
        }
    }

    data class Artist(
        val name: String,
        val description: String?,
        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail.Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?
    )

    suspend fun artist(browseId: String): Result<Artist>? {
        return browse(browseId)?.map { body ->
            Artist(
                name = body
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.title
                    ?.text ?: "Unknown",
                description = body
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.description
                    ?.text
                    ?.substringBeforeLast("\n\nFrom Wikipedia"),
                thumbnail = body
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.thumbnail
                    ?.musicThumbnailRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.getOrNull(0),
                shuffleEndpoint = body
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.playButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint,
                radioEndpoint = body
                    .header
                    ?.musicImmersiveHeaderRenderer
                    ?.startRadioButton
                    ?.buttonRenderer
                    ?.navigationEndpoint
                    ?.watchEndpoint
            )
        }
    }
}
