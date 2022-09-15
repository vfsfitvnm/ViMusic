package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.reordering.ReorderingLazyColumn
import it.vfsfitvnm.reordering.animateItemPlacement
import it.vfsfitvnm.reordering.draggedItem
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.reorder
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.PlaylistWithSongs
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.InPlaylistMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toMediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LocalPlaylistScreen(playlistId: Long) {
    val playlistWithSongs by remember(playlistId) {
        Database.playlistWithSongs(playlistId).map { it ?: PlaylistWithSongs.NotFound }
    }.collectAsState(initial = PlaylistWithSongs.Empty, context = Dispatchers.IO)

    val lazyListState = rememberLazyListState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette, typography) = LocalAppearance.current
            val menuState = LocalMenuState.current
            val binder = LocalPlayerServiceBinder.current

            val thumbnailSize = Dimensions.thumbnails.song.px

            val reorderingState = rememberReorderingState(
                lazyListState = lazyListState,
                key = playlistWithSongs.songs,
                onDragEnd = { fromIndex, toIndex ->
                    query {
                        Database.move(playlistWithSongs.playlist.id, fromIndex, toIndex)
                    }
                },
                extraItemCount = 1
            )

            var isRenaming by rememberSaveable {
                mutableStateOf(false)
            }

            if (isRenaming) {
                TextFieldDialog(
                    hintText = "Enter the playlist name",
                    initialTextInput = playlistWithSongs.playlist.name,
                    onDismiss = { isRenaming = false },
                    onDone = { text ->
                        query {
                            Database.update(playlistWithSongs.playlist.copy(name = text))
                        }
                    }
                )
            }

            var isDeleting by rememberSaveable {
                mutableStateOf(false)
            }

            if (isDeleting) {
                ConfirmationDialog(
                    text = "Do you really want to delete this playlist?",
                    onDismiss = { isDeleting = false },
                    onConfirm = {
                        query {
                            Database.delete(playlistWithSongs.playlist)
                        }
                        pop()
                    }
                )
            }

            ReorderingLazyColumn(
                reorderingState = reorderingState,
                contentPadding = LocalPlayerAwarePaddingValues.current,
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item {
                    Column {
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
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                                    .size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            BasicText(
                                text = playlistWithSongs.playlist.name,
                                style = typography.m.semiBold
                            )

                            BasicText(
                                text = "${playlistWithSongs.songs.size} songs",
                                style = typography.xxs.semiBold.secondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable(enabled = playlistWithSongs.songs.isNotEmpty()) {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(
                                            playlistWithSongs.songs
                                                .shuffled()
                                                .map(DetailedSong::asMediaItem)
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .size(20.dp)
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
                                                    text = "Enqueue",
                                                    isEnabled = playlistWithSongs.songs.isNotEmpty(),
                                                    onClick = {
                                                        menuState.hide()
                                                        binder?.player?.enqueue(
                                                            playlistWithSongs.songs.map(
                                                                DetailedSong::asMediaItem
                                                            )
                                                        )
                                                    }
                                                )

                                                MenuEntry(
                                                    icon = R.drawable.pencil,
                                                    text = "Rename",
                                                    onClick = {
                                                        menuState.hide()
                                                        isRenaming = true
                                                    }
                                                )

                                                playlistWithSongs.playlist.browseId?.let { browseId ->
                                                    MenuEntry(
                                                        icon = R.drawable.sync,
                                                        text = "Sync",
                                                        onClick = {
                                                            menuState.hide()
                                                            transaction {
                                                                runBlocking(Dispatchers.IO) {
                                                                    withContext(Dispatchers.IO) {
                                                                        YouTube.playlist(browseId)?.map {
                                                                            it.next()
                                                                        }?.map { playlist ->
                                                                            playlist.copy(items = playlist.items?.filter { it.info.endpoint != null })
                                                                        }
                                                                    }
                                                                }?.getOrNull()?.let { remotePlaylist ->
                                                                    Database.clearPlaylist(playlistWithSongs.playlist.id)

                                                                    remotePlaylist.items?.forEachIndexed { index, song ->
                                                                        song.toMediaItem(browseId, remotePlaylist)?.let { mediaItem ->
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
                                                            }
                                                        }
                                                    )
                                                }

                                                MenuEntry(
                                                    icon = R.drawable.trash,
                                                    text = "Delete",
                                                    onClick = {
                                                        menuState.hide()
                                                        isDeleting = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = playlistWithSongs.songs,
                    key = { _, song -> song.id },
                    contentType = { _, song -> song },
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                playlistWithSongs.songs.map(
                                    DetailedSong::asMediaItem
                                ), index
                            )
                        },
                        menuContent = {
                            InPlaylistMediaItemMenu(
                                playlistId = playlistId,
                                positionInPlaylist = index,
                                song = song
                            )
                        },
                        trailingContent = {
                            Image(
                                painter = painterResource(R.drawable.reorder),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                modifier = Modifier
                                    .clickable { }
                                    .reorder(
                                        reorderingState = reorderingState,
                                        index = index
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .animateItemPlacement(reorderingState = reorderingState)
                            .draggedItem(reorderingState = reorderingState, index = index)
                    )
                }
            }
        }
    }
}
