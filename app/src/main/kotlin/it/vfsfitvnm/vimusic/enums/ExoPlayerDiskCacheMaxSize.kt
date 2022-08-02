package it.vfsfitvnm.vimusic.enums

enum class ExoPlayerDiskCacheMaxSize {
    `32MB`,
    `512MB`,
    `1GB`,
    `2GB`,
    `4GB`,
    `8GB`,
    Unlimited;

    val bytes: Long
        get() = when (this) {
            `32MB` -> 32
            `512MB` -> 512
            `1GB` -> 1024
            `2GB` -> 2048
            `4GB` -> 4096
            `8GB` -> 8192
            Unlimited -> 0
        } * 1000 * 1000L
}
