package it.vfsfitvnm.synchronizedlyrics

import java.io.FileNotFoundException
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LujjjhLyrics {
    suspend fun forSong(artist: String, title: String): Result<String?>? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val artistParameter = URLEncoder.encode(artist, "UTF-8")
                val titleParameter = URLEncoder.encode(title, "UTF-8")

                URL("https://lyrics-api.lujjjh.com?artist=$artistParameter&name=$titleParameter")
                    .openConnection()
                    .getInputStream()
                    .bufferedReader()
                    .readText()
            }.recoverIfCancelled()?.recoverCatching { throwable ->
                if (throwable is FileNotFoundException) null else throw throwable
            }
        }
    }
}
