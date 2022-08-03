package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.vfsfitvnm.synchronizedlyrics.parseSentences

class SynchronizedLyrics(text: String, private val positionProvider: () -> Long) {
    val sentences = parseSentences(text)

    var index by mutableStateOf(currentIndex)
        private set

    private val currentIndex: Int
        get() {
            var index = -1
            for (item in sentences) {
                if (item.first >= positionProvider()) break
                index++
            }
            return index
        }

    fun update(): Boolean {
        val newIndex = currentIndex
        return if (newIndex != index) {
            index = newIndex
            true
        } else {
            false
        }
    }
}
