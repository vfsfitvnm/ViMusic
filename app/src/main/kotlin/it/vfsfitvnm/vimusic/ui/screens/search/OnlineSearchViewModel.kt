package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class OnlineSearchViewModel(text: String) : ViewModel() {
    var history by mutableStateOf(emptyList<SearchQuery>())
        private set

    var suggestionsResult by mutableStateOf<Result<List<String>?>?>(null)
        private set

    init {
        viewModelScope.launch {
            Database.queries("%$text%").distinctUntilChanged { old, new ->
                old.size == new.size
            }.collect {
                history = it
            }
        }

        if (text.isNotEmpty()) {
            viewModelScope.launch {
                suggestionsResult = YouTube.getSearchSuggestions(text)
            }
        }
    }
}
