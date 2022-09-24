package it.vfsfitvnm.vimusic.ui.screens.album

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.SimpleScaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun AlbumScreen(browseId: String) {
    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            SimpleScaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
            ) {
                AlbumSongList(browseId = browseId)
            }
        }
    }
}
