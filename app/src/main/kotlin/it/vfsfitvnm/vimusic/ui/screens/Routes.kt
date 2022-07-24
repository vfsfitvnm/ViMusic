package it.vfsfitvnm.vimusic.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import it.vfsfitvnm.route.Route0
import it.vfsfitvnm.route.Route1
import it.vfsfitvnm.route.RouteHandlerScope
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist

val aboutRoute = Route0("aboutRoute")
val albumRoute = Route1<String?>("albumRoute")
val appearanceSettingsRoute = Route0("appearanceSettingsRoute")
val artistRoute = Route1<String?>("artistRoute")
val backupAndRestoreRoute = Route0("backupAndRestoreRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val cacheSettingsRoute = Route0("cacheSettingsRoute")
val intentUriRoute = Route1<Uri?>("intentUriRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val otherSettingsRoute = Route0("otherSettingsRoute")
val playerSettingsRoute = Route0("playerSettingsRoute")
val playlistRoute = Route1<String?>("playlistRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")
val viewPlaylistsRoute = Route0("createPlaylistRoute")

@SuppressLint("ComposableNaming")
@Suppress("NOTHING_TO_INLINE")
@ExperimentalAnimationApi
@Composable
inline fun RouteHandlerScope.globalRoutes() {
    albumRoute { browseId ->
        AlbumScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    artistRoute { browseId ->
        ArtistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }
}
