package it.vfsfitvnm.kugou

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64String
import it.vfsfitvnm.kugou.models.DownloadLyricsResponse
import it.vfsfitvnm.kugou.models.SearchLyricsResponse
import it.vfsfitvnm.kugou.models.SearchSongResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object KuGou {
    @OptIn(ExperimentalSerializationApi::class)
    private val client by lazy {
        HttpClient(OkHttp) {
            BrowserUserAgent()

            expectSuccess = true

            install(ContentNegotiation) {
                val feature = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    encodeDefaults = true
                }

                json(feature)
                json(feature, ContentType.Text.Html)
                json(feature, ContentType.Text.Plain)
            }

            install(ContentEncoding) {
                gzip()
                deflate()
            }

            defaultRequest {
                url("https://krcs.kugou.com")
            }
        }
    }

    suspend fun lyrics(artist: String, title: String, duration: Long): Result<Lyrics?>? {
        return runCatching {
            val keyword = keyword(artist, title)
            val infoByKeyword = searchSong(keyword)

            if (infoByKeyword.isNotEmpty()) {
                var tolerance = 0

                while (tolerance <= 5) {
                    for (info in infoByKeyword) {
                        if (info.duration >= duration - tolerance && info.duration <= duration + tolerance) {
                            searchLyricsByHash(info.hash).firstOrNull()?.let { candidate ->
                                return@runCatching downloadLyrics(candidate.id, candidate.accessKey).normalize()
                            }
                        }
                    }

                    tolerance++
                }
            }

            searchLyricsByKeyword(keyword).firstOrNull()?.let { candidate ->
                return@runCatching downloadLyrics(candidate.id, candidate.accessKey).normalize()
            }

            null
        }.recoverIfCancelled()
    }

    private suspend fun downloadLyrics(id: Long, accessKey: String): Lyrics {
        return client.get("/download") {
            parameter("ver", 1)
            parameter("man", "yes")
            parameter("client", "pc")
            parameter("fmt", "lrc")
            parameter("id", id)
            parameter("accesskey", accessKey)
        }.body<DownloadLyricsResponse>().content.decodeBase64String().let(::Lyrics)
    }

    private suspend fun searchLyricsByHash(hash: String): List<SearchLyricsResponse.Candidate> {
        return client.get("/search") {
            parameter("ver", 1)
            parameter("man", "yes")
            parameter("client", "mobi")
            parameter("hash", hash)
        }.body<SearchLyricsResponse>().candidates
    }

    private suspend fun searchLyricsByKeyword(keyword: String): List<SearchLyricsResponse.Candidate> {
        return client.get("/search") {
            parameter("ver", 1)
            parameter("man", "yes")
            parameter("client", "mobi")
            url.encodedParameters.append("keyword", keyword.encodeURLParameter(spaceToPlus = false))
        }.body<SearchLyricsResponse>().candidates
    }

    private suspend fun searchSong(keyword: String): List<SearchSongResponse.Data.Info> {
        return client.get("https://mobileservice.kugou.com/api/v3/search/song") {
            parameter("version", 9108)
            parameter("plat", 0)
            parameter("pagesize", 8)
            parameter("showtype", 0)
            url.encodedParameters.append("keyword", keyword.encodeURLParameter(spaceToPlus = false))
        }.body<SearchSongResponse>().data.info
    }

    private fun keyword(artist: String, title: String): String {
        val (newTitle, featuring) = title.extract(" (feat. ", ')')

        val newArtist = (if (featuring.isEmpty()) artist else "$artist, $featuring")
            .replace(", ", "、")
            .replace(" & ", "、")
            .replace(".", "")

        return "$newArtist - $newTitle"
    }

    private fun String.extract(startDelimiter: String, endDelimiter: Char): Pair<String, String> {
        val startIndex = indexOf(startDelimiter)

        if (startIndex == -1) return this to ""

        val endIndex = indexOf(endDelimiter, startIndex)

        if (endIndex == -1) return this to ""

        return removeRange(
            startIndex,
            endIndex + 1
        ) to substring(startIndex + startDelimiter.length, endIndex)
    }

    @JvmInline
    value class Lyrics(val value: String) : CharSequence by value {
        val sentences: List<Pair<Long, String>>
            get() = mutableListOf(0L to "").apply {
                for (line in value.trim().lines()) {
                    try {
                        val position = line.take(10).run {
                            get(8).digitToInt() * 10L +
                                    get(7).digitToInt() * 100 +
                                    get(5).digitToInt() * 1000 +
                                    get(4).digitToInt() * 10000 +
                                    get(2).digitToInt() * 60 * 1000 +
                                    get(1).digitToInt() * 600 * 1000
                        }

                        add(position to line.substring(10))
                    } catch (_: Throwable) {
                    }
                }
            }

        fun normalize(): Lyrics {
            var toDrop = 0
            var maybeToDrop = 0

            val text = value.replace("\r\n", "\n").trim()

            for (line in text.lineSequence()) {
                if (line.startsWith("[ti:") ||
                    line.startsWith("[ar:") ||
                    line.startsWith("[al:") ||
                    line.startsWith("[by:") ||
                    line.startsWith("[hash:") ||
                    line.startsWith("[sign:") ||
                    line.startsWith("[qq:") ||
                    line.startsWith("[total:") ||
                    line.startsWith("[offset:") ||
                    line.startsWith("[id:") ||
                    line.containsAt("]Written by：", 9) ||
                    line.containsAt("]Lyrics by：", 9) ||
                    line.containsAt("]Composed by：", 9) ||
                    line.containsAt("]Producer：", 9) ||
                    line.containsAt("]作曲 : ", 9) ||
                    line.containsAt("]作词 : ", 9)
                ) {
                    toDrop += line.length + 1 + maybeToDrop
                    maybeToDrop = 0
                } else {
                    if (maybeToDrop == 0) {
                        maybeToDrop = line.length + 1
                    } else {
                        maybeToDrop = 0
                        break
                    }
                }
            }

            return Lyrics(text.drop(toDrop + maybeToDrop).removeHtmlEntities())
        }

        private fun String.containsAt(charSequence: CharSequence, startIndex: Int): Boolean =
            regionMatches(startIndex, charSequence, 0, charSequence.length)

        private fun String.removeHtmlEntities(): String =
            replace("&apos;", "'")
    }
}
