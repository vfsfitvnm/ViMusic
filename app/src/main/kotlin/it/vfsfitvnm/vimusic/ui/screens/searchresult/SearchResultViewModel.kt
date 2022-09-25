package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultViewModel<T : YouTube.Item>(
    private val query: String,
    private val filter: String
) : ViewModel() {
    var items by mutableStateOf(listOf<T>())

    var continuationResult by mutableStateOf<Result<String?>?>(null)

    private var job: Job? = null

    init {
        fetch()
    }

    fun fetch() {
        job?.cancel()

        viewModelScope.launch {
            val token = continuationResult?.getOrNull()

            continuationResult = null

            continuationResult = withContext(Dispatchers.IO) {
                YouTube.search(query, filter, token)
            }?.map { searchResult ->
                @Suppress("UNCHECKED_CAST")
                items = items.plus(searchResult.items as List<T>).distinctBy(YouTube.Item::key)
                searchResult.continuation
            }
        }
    }
}
