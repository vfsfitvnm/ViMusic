package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.SongCollection
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.youtubemusic.YouTube

@Stable
class Preferences(holder: SharedPreferences) : SharedPreferences by holder {
    var colorPaletteMode by preference("colorPaletteMode", ColorPaletteMode.System)
    var searchFilter by preference("searchFilter", YouTube.Item.Song.Filter.value)
    var repeatMode by preference("repeatMode", Player.REPEAT_MODE_OFF)
    var homePageSongCollection by preference("homePageSongCollection", SongCollection.MostPlayed)
    var thumbnailRoundness by preference("thumbnailRoundness", ThumbnailRoundness.Light)
    var coilDiskCacheMaxSizeBytes by preference("coilDiskCacheMaxSizeBytes", 512L * 1024 * 1024)
    var exoPlayerDiskCacheMaxSizeBytes by preference("exoPlayerDiskCacheMaxSizeBytes", 512L * 1024 * 1024)
    var displayLikeButtonInNotification by preference("displayLikeButtonInNotification", false)
    var persistentQueue by preference("persistentQueue", false)
    var skipSilence by preference("skipSilence", false)
}

val Context.preferences: Preferences
    get() = Preferences(getSharedPreferences("preferences", Context.MODE_PRIVATE))

val LocalPreferences = staticCompositionLocalOf<Preferences> { TODO() }

@Composable
fun rememberPreferences(): Preferences {
    val context = LocalContext.current
    return remember {
        context.preferences
    }
}

private fun SharedPreferences.preference(key: String, defaultValue: Boolean) =
    mutableStateOf(value = getBoolean(key, defaultValue)) {
        edit {
            putBoolean(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: Int) =
    mutableStateOf(value = getInt(key, defaultValue)) {
        edit {
            putInt(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: Long) =
    mutableStateOf(value = getLong(key, defaultValue)) {
        edit {
            putLong(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: Float) =
    mutableStateOf(value = getFloat(key, defaultValue)) {
        edit {
            putFloat(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: String) =
    mutableStateOf(value = getString(key, defaultValue)!!) {
        edit {
            putString(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: Set<String>) =
    mutableStateOf(value = getStringSet(key, defaultValue)!!) {
        edit {
            putStringSet(key, it)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: Dp) =
    mutableStateOf(value = getFloat(key, defaultValue.value).dp) {
        edit {
            putFloat(key, it.value)
        }
    }

private fun SharedPreferences.preference(key: String, defaultValue: TextUnit) =
    mutableStateOf(value = getFloat(key, defaultValue.value).sp) {
        edit {
            putFloat(key, it.value)
        }
    }

private inline fun <reified T : Enum<T>> SharedPreferences.preference(
    key: String,
    defaultValue: T
) = mutableStateOf(value = enumValueOf<T>(getString(key, defaultValue.name)!!)) {
        edit {
            putString(key, it.name)
        }
    }

private fun <T> mutableStateOf(value: T, onStructuralInequality: (newValue: T) -> Unit) =
    mutableStateOf(
        value = value,
        policy = object : SnapshotMutationPolicy<T> {
            override fun equivalent(a: T, b: T): Boolean {
                val areEquals = a == b
                if (!areEquals) onStructuralInequality(b)
                return areEquals
            }
        })
