package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.verticalDragAfterLongPressToReorder
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
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
import it.vfsfitvnm.vimusic.utils.add
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

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
            val hapticFeedback = LocalHapticFeedback.current
            val menuState = LocalMenuState.current

            val binder = LocalPlayerServiceBinder.current
            val (colorPalette, typography) = LocalAppearance.current

            val thumbnailSize = Dimensions.thumbnails.song.px

            val reorderingState = rememberReorderingState(playlistWithSongs.songs)

            var isRenaming by rememberSaveable {
                mutableStateOf(false)
            }

            if (isRenaming) {
                TextFieldDialog(
                    hintText = stringResource(R.string.enter_playlist_name),
                    initialTextInput = playlistWithSongs.playlist.name,
                    onDismiss = {
                        isRenaming = false
                    },
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
                    text = stringResource(R.string.confirm_delete_playlist),
                    onDismiss = {
                        isDeleting = false
                    },
                    onConfirm = {
                        query {
                            Database.delete(playlistWithSongs.playlist)
                        }
                        pop()
                    }
                )
            }

            LazyColumn(
                state = lazyListState,
                contentPadding = WindowInsets.systemBars.asPaddingValues().add(bottom = Dimensions.collapsedPlayer),
                modifier = Modifier
                    .background(colorPalette.background0)
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
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
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
                            text = "${playlistWithSongs.songs.size}" + stringResource(R.string.songs),
                            style = typography.xxs.semiBold.secondary
                        )
                    }
                }

                item {
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
                                                text = stringResource(R.string.enqueue),
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
                                                text = stringResource(R.string.rename),
                                                onClick = {
                                                    menuState.hide()
                                                    isRenaming = true
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.trash,
                                                text = stringResource(R.string.delete),
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

                itemsIndexed(
                    items = playlistWithSongs.songs,
                    key = { _, song -> song.id },
                    contentType = { _, song -> song },
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        swipeShow = true,
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
                                    .clickable {}
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .verticalDragAfterLongPressToReorder(
                                reorderingState = reorderingState,
                                lazyListState = lazyListState,
                                index = index,
                                onDragStart = {
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                },
                                onDragEnd = { reachedIndex ->
                                    transaction {
                                        if (index > reachedIndex) {
                                            Database.incrementSongPositions(
                                                playlistId = playlistWithSongs.playlist.id,
                                                fromPosition = reachedIndex,
                                                toPosition = index - 1
                                            )
                                        } else if (index < reachedIndex) {
                                            Database.decrementSongPositions(
                                                playlistId = playlistWithSongs.playlist.id,
                                                fromPosition = index + 1,
                                                toPosition = reachedIndex
                                            )
                                        }

                                        Database.update(
                                            SongPlaylistMap(
                                                songId = playlistWithSongs.songs[index].id,
                                                playlistId = playlistWithSongs.playlist.id,
                                                position = reachedIndex
                                            )
                                        )
                                    }
                                }
                            )
                    )
                }
            }
        }
    }
}
