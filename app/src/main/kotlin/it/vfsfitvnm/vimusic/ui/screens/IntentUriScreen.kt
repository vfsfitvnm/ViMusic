package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.relaunchableEffect
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun IntentUriScreen(uri: Uri) {

    val lazyListState = rememberLazyListState()

    var itemsResult by remember(uri) {
        mutableStateOf<Result<List<YouTube.Item.Song>>?>(null)
    }

    var playlistBrowseId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val onLoad = relaunchableEffect(uri) {
        withContext(Dispatchers.IO) {
            itemsResult = uri.getQueryParameter("list")?.let { playlistId ->
                if (playlistId.startsWith("OLAK5uy_")) {
                    YouTube.queue(playlistId)?.map { songList ->
                        songList ?: emptyList()
                    }
                } else {
                    playlistBrowseId = "VL$playlistId"
                    null
                }
            } ?: uri.getQueryParameter("v")?.let { videoId ->
                YouTube.song(videoId)?.map { song ->
                    song?.let { listOf(song) } ?: emptyList()
                }
            } ?: uri.takeIf {
                uri.host == "youtu.be"
            }?.path?.drop(1)?.let { videoId ->
                YouTube.song(videoId)?.map { song ->
                    song?.let { listOf(song) } ?: emptyList()
                }
            } ?: Result.failure(Error("Missing URL parameters"))
        }
    }

    playlistBrowseId?.let { browseId ->
        PlaylistScreen(browseId = browseId)
        return
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val menuState = LocalMenuState.current
            val (colorPalette) = LocalAppearance.current
            val binder = LocalPlayerServiceBinder.current

            val thumbnailSizePx = Dimensions.thumbnails.song.px

            var isImportingAsPlaylist by remember(uri) {
                mutableStateOf(false)
            }


            if (isImportingAsPlaylist) {
                TextFieldDialog(
                    hintText = stringResource(R.string.enter_playlist_name),
                    onDismiss = {
                        isImportingAsPlaylist = false
                    },
                    onDone = { text ->
                        menuState.hide()

                        transaction {
                            val playlistId = Database.insert(Playlist(name = text))

                            itemsResult
                                ?.getOrNull()
                                ?.map(YouTube.Item.Song::asMediaItem)
                                ?.forEachIndexed { index, mediaItem ->
                                    Database.insert(mediaItem)

                                    Database.insert(
                                        SongPlaylistMap(
                                            songId = mediaItem.mediaId,
                                            playlistId = playlistId,
                                            position = index
                                        )
                                    )
                                }
                        }
                    }
                )
            }

            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = Dimensions.collapsedPlayer),
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
            ) {
                item {
                    TopAppBar(
                        modifier = Modifier
                            .height(52.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.chevron_back),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable(onClick = pop)
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )

                        Image(
                            painter = painterResource(R.drawable.ellipsis_horizontal),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    menuState.display {
                                        Menu {
                                            MenuEntry(
                                                icon = R.drawable.enqueue,
                                                text = stringResource(R.string.enqueue),
                                                onClick = {
                                                    menuState.hide()

                                                    itemsResult
                                                        ?.getOrNull()
                                                        ?.map(YouTube.Item.Song::asMediaItem)
                                                        ?.let { mediaItems ->
                                                            binder?.player?.enqueue(
                                                                mediaItems
                                                            )
                                                        }
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.playlist,
                                                text = stringResource(R.string.import_as_playlist),
                                                onClick = {
                                                    isImportingAsPlaylist = true
                                                }
                                            )
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .size(24.dp)
                        )
                    }
                }

                itemsResult?.getOrNull()?.let { items ->
                    if (items.isEmpty()) {
                        item {
                            TextCard(icon = R.drawable.sad) {
                                Title(text = stringResource(R.string.no_songs))
                                Text(text = stringResource(R.string.other_category))
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = items,
                            contentType = { _, item -> item }
                        ) { index, item ->
                            SmallSongItem(
                                song = item,
                                thumbnailSizePx = thumbnailSizePx,
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayAtIndex(
                                        items.map(YouTube.Item.Song::asMediaItem),
                                        index
                                    )
                                }
                            )
                        }
                    }
                } ?: itemsResult?.exceptionOrNull()?.let { throwable ->
                    item {
                        LoadingOrError(
                            errorMessage = throwable.javaClass.canonicalName,
                            onRetry = onLoad
                        )
                    }
                } ?: item {
                    LoadingOrError()
                }
            }
        }
    }
}

@Composable
private fun LoadingOrError(
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    LoadingOrError(
        errorMessage = errorMessage,
        onRetry = onRetry
    ) {
        repeat(5) { index ->
            SmallSongItemShimmer(
                thumbnailSizeDp = Dimensions.thumbnails.song,
                modifier = Modifier
                    .alpha(1f - index * 0.175f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            )
        }
    }
}
