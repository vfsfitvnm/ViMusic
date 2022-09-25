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
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.mutableStatePreferenceOf
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.putEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class HomePlaylistListViewModel(application: Application) : AndroidViewModel(application) {
    var items by mutableStateOf(emptyList<PlaylistPreview>())
        private set

    var sortBy by mutableStatePreferenceOf(
        preferences.getEnum(
            playlistSortByKey,
            PlaylistSortBy.DateAdded
        )
    ) {
        preferences.edit { putEnum(playlistSortByKey, it) }
        collectItems(sortBy = it)
    }

    var sortOrder by mutableStatePreferenceOf(
        preferences.getEnum(
            playlistSortOrderKey,
            SortOrder.Ascending
        )
    ) {
        preferences.edit { putEnum(playlistSortOrderKey, it) }
        collectItems(sortOrder = it)
    }

    private var job: Job? = null

    private val preferences: SharedPreferences
        get() = getApplication<Application>().preferences

    init {
        collectItems()
    }

    private fun collectItems(
        sortBy: PlaylistSortBy = this.sortBy,
        sortOrder: SortOrder = this.sortOrder
    ) {
        job?.cancel()
        job = viewModelScope.launch {
            Database.playlistPreviews(sortBy, sortOrder).flowOn(Dispatchers.IO).collect {
                items = it
            }
        }
    }
}
