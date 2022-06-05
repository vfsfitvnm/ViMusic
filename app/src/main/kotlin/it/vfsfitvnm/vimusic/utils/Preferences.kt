package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.SongCollection
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences as DataStorePreferences


@Immutable
data class Preferences(
    val isReady: Boolean,
    val colorPaletteMode: ColorPaletteMode,
    val onColorPaletteModeChange: (ColorPaletteMode) -> Unit,
    val searchFilter: String,
    val onSearchFilterChange: (String) -> Unit,
    val repeatMode: Int,
    val onRepeatModeChange: (Int) -> Unit,
    val homePageSongCollection: SongCollection,
    val onHomePageSongCollectionChange: (SongCollection) -> Unit,
    val thumbnailRoundness: ThumbnailRoundness,
    val onThumbnailRoundnessChange: (ThumbnailRoundness) -> Unit,
) {
    constructor(
        isReady: Boolean,
        colorPaletteMode: ColorPaletteMode? = null,
        onColorPaletteModeChange: (ColorPaletteMode) -> Unit = {},
        searchFilter: String? = null,
        onSearchFilterChange: (String) -> Unit = {},
        repeatMode: Int? = null,
        onRepeatModeChange: (Int) -> Unit = {},
        homePageSongCollection: SongCollection? = null,
        onHomePageSongCollectionChange: (SongCollection) -> Unit = {},
        thumbnailRoundness: ThumbnailRoundness? = null,
        onThumbnailRoundnessChange: (ThumbnailRoundness) -> Unit = {},
    ) : this(
        isReady = isReady,
        colorPaletteMode = colorPaletteMode ?: ColorPaletteMode.System,
        onColorPaletteModeChange = onColorPaletteModeChange,
        searchFilter = searchFilter ?: YouTube.Item.Song.Filter.value,
        onSearchFilterChange = onSearchFilterChange,
        repeatMode = repeatMode ?: Player.REPEAT_MODE_OFF,
        onRepeatModeChange = onRepeatModeChange,
        homePageSongCollection = homePageSongCollection ?: SongCollection.MostPlayed,
        onHomePageSongCollectionChange = onHomePageSongCollectionChange,
        thumbnailRoundness = thumbnailRoundness ?: ThumbnailRoundness.Light,
        onThumbnailRoundnessChange = onThumbnailRoundnessChange
    )

    companion object {
        val Default = Preferences(isReady = false)
    }
}

val LocalPreferences = staticCompositionLocalOf { Preferences.Default }

private val colorPaletteModeKey = stringPreferencesKey("colorPaletteMode")
private val searchFilterKey = stringPreferencesKey("searchFilter")
private val repeatModeKey = intPreferencesKey("repeatMode")
private val homePageSongCollectionKey = stringPreferencesKey("homePageSongCollection")
private val thumbnailRoundnessKey = stringPreferencesKey("thumbnailRoundness")

@Composable
fun rememberPreferences(dataStore: DataStore<DataStorePreferences>): State<Preferences> {
    val coroutineScope = rememberCoroutineScope()

    return remember(dataStore, coroutineScope) {
        dataStore.data.map { preferences ->
            Preferences(
                isReady = true,
                colorPaletteMode = preferences[colorPaletteModeKey]?.let { enumValueOf<ColorPaletteMode>(it) },
                onColorPaletteModeChange = { colorPaletteMode ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[colorPaletteModeKey] = colorPaletteMode.name
                        }
                    }
                },
                searchFilter = preferences[searchFilterKey],
                onSearchFilterChange = { searchFilter ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[searchFilterKey] = searchFilter
                        }
                    }
                },
                repeatMode = preferences[repeatModeKey],
                onRepeatModeChange = { repeatMode ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[repeatModeKey] = repeatMode
                        }
                    }
                },
                homePageSongCollection = preferences[homePageSongCollectionKey]?.let { enumValueOf<SongCollection>(it) },
                onHomePageSongCollectionChange = { homePageSongCollection ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[homePageSongCollectionKey] = homePageSongCollection.name
                        }
                    }
                },
                thumbnailRoundness = preferences[thumbnailRoundnessKey]?.let { enumValueOf<ThumbnailRoundness>(it) },
                onThumbnailRoundnessChange = { thumbnailRoundness ->
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[thumbnailRoundnessKey] = thumbnailRoundness.name
                        }
                    }
                },
            )
        }
    }.collectAsState(initial = Preferences.Default, context = Dispatchers.IO)
}
