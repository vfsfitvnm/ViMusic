package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.DetailedSong
import kotlinx.coroutines.launch

class LibrarySearchTabViewModel(text: String) : ViewModel() {
    var items by mutableStateOf(emptyList<DetailedSong>())
        private set

    init {
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                Database.search("%$text%").collect {
                    items = it
                }
            }
        }
    }
}
