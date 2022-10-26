package it.vfsfitvnm.vimusic.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import it.vfsfitvnm.compose.routing.Route0
import it.vfsfitvnm.compose.routing.Route1
import it.vfsfitvnm.compose.routing.RouteHandlerScope
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.ui.screens.album.AlbumScreen
import it.vfsfitvnm.vimusic.ui.screens.artist.ArtistScreen
import it.vfsfitvnm.vimusic.ui.screens.playlist.PlaylistScreen

val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val playlistRoute = Route1<String?>("playlistRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@SuppressLint("ComposableNaming")
@Suppress("NOTHING_TO_INLINE")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
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

    playlistRoute { browseId ->
        PlaylistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }
}
