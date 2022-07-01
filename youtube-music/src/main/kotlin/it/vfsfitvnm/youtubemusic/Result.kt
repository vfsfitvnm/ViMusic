package it.vfsfitvnm.youtubemusic

import io.ktor.utils.io.*


internal fun <T> Result<T>.recoverIfCancelled(): Result<T>? {
    return when (exceptionOrNull()) {
        is CancellationException -> null
        else -> this
    }
}
