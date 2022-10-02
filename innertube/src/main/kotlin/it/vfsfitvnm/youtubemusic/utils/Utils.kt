package it.vfsfitvnm.youtubemusic.utils

import io.ktor.utils.io.CancellationException
import it.vfsfitvnm.youtubemusic.models.SectionListRenderer

internal fun SectionListRenderer.findSectionByTitle(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
            val title = content
                .musicCarouselShelfRenderer
                ?.header
                ?.musicCarouselShelfBasicHeaderRenderer
                ?.title
                ?: content
                    .musicShelfRenderer
                    ?.title

            title
                ?.runs
                ?.firstOrNull()
                ?.text == text
        }
}

internal fun SectionListRenderer.findSectionByStrapline(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.strapline
            ?.runs
            ?.firstOrNull()
            ?.text == text
        }
}

internal inline fun <R> runCatchingNonCancellable(block: () -> R): Result<R>? {
    return Result.success(block())
//    val result = runCatching(block)
//    return when (val ex = result.exceptionOrNull()) {
//        is CancellationException -> null
//        else -> result
//    }
}
