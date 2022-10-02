package it.vfsfitvnm.youtubemusic.models.bodies

import it.vfsfitvnm.youtubemusic.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class NextBody(
    val context: Context = Context.DefaultWeb,
    val videoId: String?,
    val isAudioOnly: Boolean = true,
    val playlistId: String? = null,
    val tunerSettingValue: String = "AUTOMIX_SETTING_NORMAL",
    val index: Int? = null,
    val params: String? = null,
    val playlistSetVideoId: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs = WatchEndpointMusicSupportedConfigs(
        musicVideoType = "MUSIC_VIDEO_TYPE_ATV"
    )
) {
    @Serializable
    data class WatchEndpointMusicSupportedConfigs(
        val musicVideoType: String
    )
}
