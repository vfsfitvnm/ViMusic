package it.vfsfitvnm.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ContinuationResponse(
    val continuationContents: ContinuationContents?,
) {
    @Serializable
    data class ContinuationContents(
        @JsonNames("musicPlaylistShelfContinuation")
        val musicShelfContinuation: MusicShelfRenderer?,
        val playlistPanelContinuation: NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer?,
    )
}
