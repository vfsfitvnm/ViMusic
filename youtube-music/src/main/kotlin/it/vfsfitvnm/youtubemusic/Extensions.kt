package it.vfsfitvnm.youtubemusic

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.network.*
import io.ktor.utils.io.*


suspend inline fun <reified T> Outcome<HttpResponse>.bodyCatching(): Outcome<T> {
    return when (this) {
        is Outcome.Success -> value.bodyCatching()
        is Outcome.Recovered -> value.bodyCatching()
        is Outcome.Initial -> this
        is Outcome.Loading -> this
        is Outcome.Error -> this
    }
}

suspend inline fun HttpClient.postCatching(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Outcome<HttpResponse> {
    return runCatching {
        Outcome.Success(post(urlString, block))
    }.getOrElse { throwable ->
        when (throwable) {
            is CancellationException -> Outcome.Loading
            is UnresolvedAddressException -> Outcome.Error.Network
            else -> Outcome.Error.Unhandled(throwable)
        }
    }
}

suspend inline fun <reified T> HttpResponse.bodyCatching(): Outcome<T> {
    return runCatching {
        Outcome.Success(body<T>())
    }.getOrElse { throwable ->
        Outcome.Error.Unhandled(throwable)
    }
}