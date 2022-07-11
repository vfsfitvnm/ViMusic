package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.enums.*
import it.vfsfitvnm.youtubemusic.YouTube


@Stable
class Preferences(
    private val edit: (action: SharedPreferences.Editor.() -> Unit) -> Unit,
    initialIsFirstLaunch: Boolean,
    initialSongSortBy: SongSortBy,
    initialSongSortOrder: SortOrder,
    initialColorPaletteMode: ColorPaletteMode,
    initialSearchFilter: String,
    initialRepeatMode: Int,
    initialThumbnailRoundness: ThumbnailRoundness,
    initialCoilDiskCacheMaxSizeBytes: Long,
    initialExoPlayerDiskCacheMaxSizeBytes: Long,
    initialSkipSilence: Boolean,
    initialVolumeNormalization: Boolean,
    initialPersistentQueue: Boolean,
    initialIsInvincibilityEnabled: Boolean,
) {
    constructor(preferences: SharedPreferences) : this(
        edit = { action: SharedPreferences.Editor.() -> Unit ->
           preferences.edit(action = action)
        },
        initialIsFirstLaunch = preferences.getBoolean(Keys.isFirstLaunch, true),
        initialSongSortBy = preferences.getEnum(Keys.songSortBy, SongSortBy.DateAdded),
        initialSongSortOrder = preferences.getEnum(Keys.songSortOrder, SortOrder.Descending),
        initialColorPaletteMode = preferences.getEnum(Keys.colorPaletteMode, ColorPaletteMode.System),
        initialSearchFilter = preferences.getString(Keys.searchFilter, YouTube.Item.Song.Filter.value)!!,
        initialRepeatMode = preferences.getInt(Keys.repeatMode, Player.REPEAT_MODE_OFF),
        initialThumbnailRoundness = preferences.getEnum(Keys.thumbnailRoundness, ThumbnailRoundness.Light),
        initialCoilDiskCacheMaxSizeBytes = preferences.getLong(Keys.coilDiskCacheMaxSizeBytes, 512L * 1024 * 1024),
        initialExoPlayerDiskCacheMaxSizeBytes = preferences.getLong(Keys.exoPlayerDiskCacheMaxSizeBytes, 512L * 1024 * 1024),
        initialSkipSilence = preferences.getBoolean(Keys.skipSilence, false),
        initialVolumeNormalization = preferences.getBoolean(Keys.volumeNormalization, false),
        initialPersistentQueue = preferences.getBoolean(Keys.persistentQueue, false),
        initialIsInvincibilityEnabled = preferences.getBoolean(Keys.isInvincibilityEnabled, false),
    )

    var isFirstLaunch = initialIsFirstLaunch
        set(value) = edit { putBoolean(Keys.isFirstLaunch, value) }

    var songSortBy = initialSongSortBy
        set(value) = edit { putEnum(Keys.songSortBy, value) }

    var songSortOrder = initialSongSortOrder
        set(value) = edit { putEnum(Keys.songSortOrder, value) }

    var colorPaletteMode = initialColorPaletteMode
        set(value) = edit { putEnum(Keys.colorPaletteMode, value) }

    var searchFilter = initialSearchFilter
        set(value) = edit { putString(Keys.searchFilter, value) }

    var repeatMode = initialRepeatMode
        set(value) = edit { putInt(Keys.repeatMode, value) }

    var thumbnailRoundness = initialThumbnailRoundness
        set(value) = edit { putEnum(Keys.thumbnailRoundness, value) }

    var coilDiskCacheMaxSizeBytes = initialCoilDiskCacheMaxSizeBytes
        set(value) = edit { putLong(Keys.coilDiskCacheMaxSizeBytes, value) }

    var exoPlayerDiskCacheMaxSizeBytes = initialExoPlayerDiskCacheMaxSizeBytes
        set(value) = edit { putLong(Keys.exoPlayerDiskCacheMaxSizeBytes, value) }

    var skipSilence = initialSkipSilence
        set(value) = edit { putBoolean(Keys.skipSilence, value) }

    var volumeNormalization = initialVolumeNormalization
        set(value) = edit { putBoolean(Keys.volumeNormalization, value) }

    var persistentQueue = initialPersistentQueue
        set(value) = edit { putBoolean(Keys.persistentQueue, value) }

    var isInvincibilityEnabled = initialIsInvincibilityEnabled
        set(value) = edit { putBoolean(Keys.isInvincibilityEnabled, value) }

    object Keys {
        const val isFirstLaunch = "isFirstLaunch"
        const val songSortOrder = "songSortOrder"
        const val songSortBy = "songSortBy"
        const val colorPaletteMode = "colorPaletteMode"
        const val searchFilter = "searchFilter"
        const val repeatMode = "repeatMode"
        const val thumbnailRoundness = "thumbnailRoundness"
        const val coilDiskCacheMaxSizeBytes = "coilDiskCacheMaxSizeBytes"
        const val exoPlayerDiskCacheMaxSizeBytes = "exoPlayerDiskCacheMaxSizeBytes"
        const val skipSilence = "skipSilence"
        const val volumeNormalization = "volumeNormalization"
        const val persistentQueue = "persistentQueue"
        const val isInvincibilityEnabled = "isInvincibilityEnabled"
    }

    companion object {
        const val fileName = "preferences"

        context(Context)
        operator fun invoke() =
            Preferences(getSharedPreferences(fileName, Context.MODE_PRIVATE))
    }
}

val LocalPreferences = staticCompositionLocalOf<Preferences> { TODO() }

@Composable
fun rememberPreferences(): Preferences {
    val context = LocalContext.current
    var preferences by remember {
        mutableStateOf(context.run { Preferences() })
    }

    DisposableEffect(Unit) {
        val holder = context.getSharedPreferences(Preferences.fileName, Context.MODE_PRIVATE)

        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
                preferences = Preferences(sharedPreferences)
            }

        holder.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            holder.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return preferences
}

private inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T =
    getString(key, null)?.let {
        try {
            enumValueOf<T>(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue

private inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T) =
    putString(key, value.name)

