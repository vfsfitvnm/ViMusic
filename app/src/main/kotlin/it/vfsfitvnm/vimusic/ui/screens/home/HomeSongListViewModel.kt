package it.vfsfitvnm.vimusic.ui.screens.home

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.mutableStatePreferenceOf
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.putEnum
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class HomeSongListViewModel(application: Application) : AndroidViewModel(application) {
    var items by mutableStateOf(emptyList<DetailedSong>())
        private set

    var sortBy by mutableStatePreferenceOf(
        preferences.getEnum(
            songSortByKey,
            SongSortBy.DateAdded
        )
    ) {
        preferences.edit { putEnum(songSortByKey, it) }
        collectItems(sortBy = it)
    }

    var sortOrder by mutableStatePreferenceOf(
        preferences.getEnum(
            songSortOrderKey,
            SortOrder.Ascending
        )
    ) {
        preferences.edit { putEnum(songSortOrderKey, it) }
        collectItems(sortOrder = it)
    }

    private var job: Job? = null

    private val preferences: SharedPreferences
        get() = getApplication<Application>().preferences

    init {
        collectItems()
    }

    private fun collectItems(sortBy: SongSortBy = this.sortBy, sortOrder: SortOrder = this.sortOrder) {
        job?.cancel()
        job = viewModelScope.launch {
            Database.songs(sortBy, sortOrder).flowOn(Dispatchers.IO).collect {
                items = it
            }
        }
    }
}
