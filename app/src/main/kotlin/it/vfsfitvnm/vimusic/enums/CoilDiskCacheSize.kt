package it.vfsfitvnm.vimusic.enums

enum class CoilDiskCacheMaxSize {
    `128MB`,
    `256MB`,
    `512MB`,
    `1GB`,
    `2GB`;

    val bytes: Long
        get() = when (this) {
            `128MB` -> 128
            `256MB` -> 256
            `512MB` -> 512
            `1GB` -> 1024
            `2GB` -> 2048
        } * 1000 * 1000L
}
