package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SynchronizedLyrics(val sentences: List<Pair<Long, String>>, private val positionProvider: () -> Long) {
    var index by mutableStateOf(currentIndex)
        private set

    private val currentIndex: Int
        get() {
            var index = -1
            for (item in sentences) {
                if (item.first >= positionProvider()) break
                index++
            }
            return if (index == -1) 0 else index
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
