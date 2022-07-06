package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import it.vfsfitvnm.route.Route0
import it.vfsfitvnm.route.Route1
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist


@Composable
fun rememberIntentUriRoute(): Route1<Uri?> {
    val uri = rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    return remember {
        Route1("IntentUriRoute", uri)
    }
}

@Composable
fun rememberPlaylistRoute(): Route1<String?> {
    val browseId = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    return remember {
        Route1("PlaylistRoute", browseId)
    }
}

@Composable
fun rememberAlbumRoute(): Route1<String?> {
    val browseId = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    return remember {
        Route1("AlbumRoute", browseId)
    }
}

@Composable
fun rememberArtistRoute(): Route1<String?> {
    val browseId = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    return remember {
        Route1("ArtistRoute", browseId)
    }
}

@Composable
fun rememberLocalPlaylistRoute(): Route1<Long?> {
    val playlistId = rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    return remember {
        Route1("LocalPlaylistRoute", playlistId)
    }
}

@Composable
fun rememberBuiltInPlaylistRoute(): Route1<BuiltInPlaylist> {
    val playlistType = rememberSaveable {
        mutableStateOf(BuiltInPlaylist.Favorites)
    }
    return remember {
        Route1("BuiltInPlaylistRoute", playlistType)
    }
}

@Composable
fun rememberSearchRoute(): Route1<String> {
    val initialTextInput = remember {
        mutableStateOf("")
    }
    return remember {
        Route1("SearchRoute", initialTextInput)
    }
}

@Composable
fun rememberCreatePlaylistRoute(): Route0 {
    return remember {
        Route0("CreatePlaylistRoute")
    }
}

@Composable
fun rememberSearchResultRoute(): Route1<String> {
    val searchQuery = rememberSaveable {
        mutableStateOf("")
    }
    return remember {
        Route1("SearchResultRoute", searchQuery)
    }
}

@Composable
fun rememberLyricsRoute(): Route0 {
    return remember {
        Route0("LyricsRoute")
    }
}

@Composable
fun rememberSettingsRoute(): Route0 {
    return remember {
        Route0("SettingsRoute")
    }
}
