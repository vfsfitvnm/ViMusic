package it.vfsfitvnm.vimusic.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import it.vfsfitvnm.route.Route0
import it.vfsfitvnm.route.Route1
import it.vfsfitvnm.route.RouteHandlerScope
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.ui.screens.album.AlbumScreen

val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val intentUriRoute = Route1<Uri?>("intentUriRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
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
