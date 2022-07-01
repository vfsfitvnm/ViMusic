package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.SongCollection
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.youtubemusic.YouTube


@Stable
class Preferences(
    private val edit: (action: SharedPreferences.Editor.() -> Unit) -> Unit,
    initialColorPaletteMode: ColorPaletteMode,
    initialSearchFilter: String,
    initialRepeatMode: Int,
    initialHomePageSongCollection: SongCollection,
    initialThumbnailRoundness: ThumbnailRoundness,
    initialCoilDiskCacheMaxSizeBytes: Long,
    initialExoPlayerDiskCacheMaxSizeBytes: Long,
    initialSkipSilence: Boolean,
    initialVolumeNormalization: Boolean,
    initialPersistentQueue: Boolean,
) {
    constructor(preferences: SharedPreferences) : this(
        edit = { action: SharedPreferences.Editor.() -> Unit ->
           preferences.edit(action = action)
        },
        initialColorPaletteMode = preferences.getEnum(Keys.colorPaletteMode, ColorPaletteMode.System),
        initialSearchFilter = preferences.getString(Keys.searchFilter, YouTube.Item.Song.Filter.value)!!,
        initialRepeatMode = preferences.getInt(Keys.repeatMode, Player.REPEAT_MODE_OFF),
        initialHomePageSongCollection = preferences.getEnum(Keys.homePageSongCollection, SongCollection.History),
        initialThumbnailRoundness = preferences.getEnum(Keys.thumbnailRoundness, ThumbnailRoundness.Light),
        initialCoilDiskCacheMaxSizeBytes = preferences.getLong(Keys.coilDiskCacheMaxSizeBytes, 512L * 1024 * 1024),
        initialExoPlayerDiskCacheMaxSizeBytes = preferences.getLong(Keys.exoPlayerDiskCacheMaxSizeBytes, 512L * 1024 * 1024),
        initialSkipSilence = preferences.getBoolean(Keys.skipSilence, false),
        initialVolumeNormalization = preferences.getBoolean(Keys.volumeNormalization, false),
        initialPersistentQueue = preferences.getBoolean(Keys.persistentQueue, false)
    )

    var colorPaletteMode = initialColorPaletteMode
        set(value) = edit { putEnum(Keys.colorPaletteMode, value) }

    var searchFilter = initialSearchFilter
        set(value) = edit { putString(Keys.searchFilter, value) }

    var repeatMode = initialRepeatMode
        set(value) = edit { putInt(Keys.repeatMode, value) }

    var homePageSongCollection = initialHomePageSongCollection
        set(value) = edit { putEnum(Keys.homePageSongCollection, value) }

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

    private object Keys {
        const val colorPaletteMode = "colorPaletteMode"
        const val searchFilter = "searchFilter"
        const val repeatMode = "repeatMode"
        const val homePageSongCollection = "homePageSongCollection"
        const val thumbnailRoundness = "thumbnailRoundness"
        const val coilDiskCacheMaxSizeBytes = "coilDiskCacheMaxSizeBytes"
        const val exoPlayerDiskCacheMaxSizeBytes = "exoPlayerDiskCacheMaxSizeBytes"
        const val skipSilence = "skipSilence"
        const val volumeNormalization = "volumeNormalization"
        const val persistentQueue = "persistentQueue"
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

