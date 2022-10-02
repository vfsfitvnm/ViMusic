package it.vfsfitvnm.youtubemusic.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.BrowseResponse
import it.vfsfitvnm.youtubemusic.models.ContinuationResponse
import it.vfsfitvnm.youtubemusic.models.MusicShelfRenderer
import it.vfsfitvnm.youtubemusic.models.bodies.BrowseBody
import it.vfsfitvnm.youtubemusic.models.bodies.ContinuationBody
import it.vfsfitvnm.youtubemusic.utils.from
import it.vfsfitvnm.youtubemusic.utils.runCatchingNonCancellable

suspend fun Innertube.playlistPage(body: BrowseBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.musicPlaylistShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask),header.musicDetailHeaderRenderer(title,subtitle,thumbnail),microformat")
    }.body<BrowseResponse>()

    val musicDetailHeaderRenderer = response
        .header
        ?.musicDetailHeaderRenderer

    val musicShelfRenderer = response
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

    Innertube.PlaylistOrAlbumPage(
        title = musicDetailHeaderRenderer
            ?.title
            ?.text,
        thumbnail = musicDetailHeaderRenderer
            ?.thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull(),
        authors = musicDetailHeaderRenderer
            ?.subtitle
            ?.splitBySeparator()
            ?.getOrNull(1)
            ?.map(Innertube::Info),
        year = musicDetailHeaderRenderer
            ?.subtitle
            ?.splitBySeparator()
            ?.getOrNull(2)
            ?.firstOrNull()
            ?.text,
        url = response
            .microformat
            ?.microformatDataRenderer
            ?.urlCanonical,
        songsPage = musicShelfRenderer
            ?.toSongsPage()
    )
}

suspend fun Innertube.playlistPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        mask("continuationContents.musicPlaylistShelfContinuation(continuations,contents.$musicResponsiveListItemRendererMask)")
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

private fun MusicShelfRenderer?.toSongsPage() =
    Innertube.ItemsPage(
        items = this
            ?.contents
            ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
            ?.mapNotNull(Innertube.SongItem::from),
        continuation = this
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
    )
