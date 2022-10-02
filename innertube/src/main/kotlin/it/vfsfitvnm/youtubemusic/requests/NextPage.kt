package it.vfsfitvnm.youtubemusic.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.ContinuationResponse
import it.vfsfitvnm.youtubemusic.models.NextResponse
import it.vfsfitvnm.youtubemusic.models.bodies.ContinuationBody
import it.vfsfitvnm.youtubemusic.models.bodies.NextBody
import it.vfsfitvnm.youtubemusic.utils.from
import it.vfsfitvnm.youtubemusic.utils.runCatchingNonCancellable



suspend fun Innertube.nextPage(body: NextBody): Result<Innertube.NextPage>? =
    runCatchingNonCancellable {
        val response = client.post(next) {
            setBody(body)
            mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer.content.musicQueueRenderer.content.playlistPanelRenderer(continuations,contents(automixPreviewVideoRenderer,$playlistPanelVideoRendererMask))")
        }.body<NextResponse>()

        val tabs = response
            .contents
            ?.singleColumnMusicWatchNextResultsRenderer
            ?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer
            ?.tabs

        val playlistPanelRenderer = tabs
            ?.getOrNull(0)
            ?.tabRenderer
            ?.content
            ?.musicQueueRenderer
            ?.content
            ?.playlistPanelRenderer

        if (body.playlistId == null) {
            val endpoint = playlistPanelRenderer
                ?.contents
                ?.lastOrNull()
                ?.automixPreviewVideoRenderer
                ?.content
                ?.automixPlaylistVideoRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint

            if (endpoint != null) {
                return nextPage(
                    body.copy(
                        playlistId = endpoint.playlistId,
                        params = endpoint.params
                    )
                )
            }
        }

        Innertube.NextPage(
            playlistId = body.playlistId,
            playlistSetVideoId = body.playlistSetVideoId,
            params = body.params,
            itemsPage = playlistPanelRenderer
                ?.toSongsPage()
        )
    }

suspend fun Innertube.nextPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(next) {
        setBody(body)
        mask("continuationContents.playlistPanelContinuation(continuations,contents.$playlistPanelVideoRendererMask)")
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.playlistPanelContinuation
        ?.toSongsPage()
}

private fun NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer?.toSongsPage() =
    Innertube.ItemsPage(
        items = this
            ?.contents
            ?.mapNotNull(NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer.Content::playlistPanelVideoRenderer)
            ?.mapNotNull(Innertube.SongItem::from),
        continuation = this
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
    )
