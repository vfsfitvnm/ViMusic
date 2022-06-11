package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongInPlaylist
import it.vfsfitvnm.vimusic.services.StopRadioCommand
import it.vfsfitvnm.vimusic.ui.components.Error
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.Message
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuCloseButton
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.toNullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun IntentUriScreen(uri: Uri) {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val lazyListState = rememberLazyListState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val menuState = LocalMenuState.current
            val colorPalette = LocalColorPalette.current
            val density = LocalDensity.current
            val player = LocalYoutubePlayer.current

            val coroutineScope = rememberCoroutineScope()
            val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

            var items by remember(uri) {
                mutableStateOf<Outcome<List<YouTube.Item.Song>>>(Outcome.Loading)
            }

            val onLoad = relaunchableEffect(uri) {
                items = withContext(Dispatchers.IO) {
                    uri.getQueryParameter("list")?.let { playlistId ->
                        YouTube.queue(playlistId).toNullable()?.map { songList ->
                            songList
                        }
                    } ?: uri.getQueryParameter("v")?.let { videoId ->
                        YouTube.song(videoId).toNullable()?.map { listOf(it) }
                    } ?: Outcome.Error.Network
                }
            }

            var isImportingAsPlaylist by remember(uri) {
                mutableStateOf(false)
            }

            if (isImportingAsPlaylist) {
                TextFieldDialog(
                    hintText = "Enter the playlist name",
                    onDismiss = {
                        isImportingAsPlaylist = false
                    },
                    onDone = { text ->
                        menuState.hide()

                        coroutineScope.launch(Dispatchers.IO) {
                            Database.internal.runInTransaction {
                                val playlistId = Database.insert(Playlist(name = text))

                                items.valueOrNull
                                    ?.map(YouTube.Item.Song::asMediaItem)
                                    ?.forEachIndexed { index, mediaItem ->
                                        if (Database.song(mediaItem.mediaId) == null) {
                                            Database.insert(mediaItem)
                                        }

                                        Database.insert(
                                            SongInPlaylist(
                                                songId = mediaItem.mediaId,
                                                playlistId = playlistId,
                                                position = index
                                            )
                                        )
                                    }
                            }
                        }
                    }
                )
            }

            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 64.dp),
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
                                            MenuCloseButton(onClick = menuState::hide)

                                            MenuEntry(
                                                icon = R.drawable.time,
                                                text = "Enqueue",
                                                enabled = player?.playbackState == Player.STATE_READY,
                                                onClick = {
                                                    menuState.hide()

                                                    items.valueOrNull
                                                        ?.map(YouTube.Item.Song::asMediaItem)
                                                        ?.let { mediaItems ->
                                                            player?.mediaController?.enqueue(
                                                                mediaItems
                                                            )
                                                        }
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.list,
                                                text = "Import as playlist",
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

                when (val currentItems = items) {
                    is Outcome.Error -> item {
                        Error(
                            error = currentItems,
                            onRetry = onLoad,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                    is Outcome.Recovered -> item {
                        Error(
                            error = currentItems.error,
                            onRetry = onLoad,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                    is Outcome.Loading, is Outcome.Initial -> items(count = 5) { index ->
                        SmallSongItemShimmer(
                            shimmer = shimmer,
                            thumbnailSizeDp = 54.dp,
                            modifier = Modifier
                                .alpha(1f - index * 0.175f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                    is Outcome.Success -> {
                        if (currentItems.value.isEmpty()) {
                            item {
                                Message(
                                    text = "No songs were found",
                                    modifier = Modifier
                                )
                            }
                        } else {
                            itemsIndexed(
                                items = currentItems.value,
                                contentType = { _, item -> item }
                            ) { index, item ->
                                SmallSongItem(
                                    song = item,
                                    thumbnailSizePx = density.run { 54.dp.roundToPx() },
                                    onClick = {
                                        player?.mediaController?.let {
                                            it.sendCustomCommand(StopRadioCommand, Bundle.EMPTY)
                                            it.forcePlayAtIndex(currentItems.value.map(YouTube.Item.Song::asMediaItem), index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
