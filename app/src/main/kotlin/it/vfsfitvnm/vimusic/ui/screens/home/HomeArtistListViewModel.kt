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
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.utils.artistSortByKey
import it.vfsfitvnm.vimusic.utils.artistSortOrderKey
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.mutableStatePreferenceOf
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.putEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class HomeArtistListViewModel(application: Application) : AndroidViewModel(application) {
    var items by mutableStateOf(emptyList<Artist>())
        private set

    var sortBy by mutableStatePreferenceOf(
        preferences.getEnum(
            artistSortByKey,
            ArtistSortBy.DateAdded
        )
    ) {
        preferences.edit { putEnum(artistSortByKey, it) }
        collectItems(sortBy = it)
    }

    var sortOrder by mutableStatePreferenceOf(
        preferences.getEnum(
            artistSortOrderKey,
            SortOrder.Ascending
        )
    ) {
        preferences.edit { putEnum(artistSortOrderKey, it) }
        collectItems(sortOrder = it)
    }

    private var job: Job? = null

    private val preferences: SharedPreferences
        get() = getApplication<Application>().preferences

    init {
        collectItems()
    }

    private fun collectItems(sortBy: ArtistSortBy = this.sortBy, sortOrder: SortOrder = this.sortOrder) {
        job?.cancel()
        job = viewModelScope.launch {
            Database.artists(sortBy, sortOrder).flowOn(Dispatchers.IO).collect {
                items = it
            }
        }
    }
}
