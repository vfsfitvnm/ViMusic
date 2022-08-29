package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus,
    val playerConfig: PlayerConfig?,
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig?
    ) {
        @Serializable
        data class AudioConfig(
            val loudnessDb: Double?,
            val perceptualLoudnessDb: Double?
        )
    }

    @Serializable
    data class StreamingData(
        val adaptiveFormats: List<AdaptiveFormat>,
        val expiresInSeconds: String
    ) {
        @Serializable
        data class AdaptiveFormat(
            val itag: Int,
            val mimeType: String,
            val bitrate: Long?,
            val averageBitrate: Long?,
            val contentLength: Long?,
            val audioQuality: String?,
            val approxDurationMs: Long?,
            val lastModified: Long?,
            val loudnessDb: Double?,
            val audioSampleRate: Int?,
            val url: String?,
        )
    }

    @Serializable
    data class VideoDetails(
        val author: String,
        val channelId: String,
        val lengthSeconds: String,
        val thumbnail: ThumbnailRenderer.MusicThumbnailRenderer.Thumbnail,
        val title: String,
        val videoId: String
    )
}
