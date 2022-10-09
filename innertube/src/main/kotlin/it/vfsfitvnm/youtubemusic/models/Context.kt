package it.vfsfitvnm.youtubemusic.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String,
        val hl: String = "en",
        val visitorData: String? = null,
        val androidSdkVersion: Int? = null
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    companion object {
        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220918",
                platform = "DESKTOP",
                visitorData = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
            )
        )

        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID",
                clientVersion = "17.36.4",
                platform = "MOBILE",
                androidSdkVersion = 31,
            )
        )

        val DefaultAgeRestrictionBypass = Context(
            client = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                platform = "TV",
                visitorData = null,
            )
        )
    }
}
