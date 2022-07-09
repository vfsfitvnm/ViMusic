package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.verticalDragAfterLongPressToReorder
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.*
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.PlaylistWithSongs
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map


@ExperimentalAnimationApi
@Composable
fun LocalPlaylistScreen(
    playlistId: Long,
) {
    val playlistWithSongs by remember(playlistId) {
        Database.playlistWithSongs(playlistId).map { it ?: PlaylistWithSongs.NotFound }
    }.collectAsState(initial = PlaylistWithSongs.Empty, context = Dispatchers.IO)

    val lazyListState = rememberLazyListState()

    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    RouteHandler(listenToGlobalEmitter = true) {
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

        host {
            val hapticFeedback = LocalHapticFeedback.current
            val menuState = LocalMenuState.current

            val binder = LocalPlayerServiceBinder.current
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current

            val thumbnailSize = Dimensions.thumbnails.song.px

            val reorderingState = rememberReorderingState(playlistWithSongs.songs)

            var isRenaming by rememberSaveable {
                mutableStateOf(false)
            }

            if (isRenaming) {
                TextFieldDialog(
                    hintText = "Enter the playlist name",
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
                    text = "Do you really want to delete this playlist?",
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
                                .padding(vertical = 8.dp, horizontal = 16.dp)
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
                                                text = "Enqueue",
                                                isEnabled = playlistWithSongs.songs.isNotEmpty(),
                                                onClick = {
                                                    menuState.hide()
                                                    binder?.player?.enqueue(playlistWithSongs.songs.map(DetailedSong::asMediaItem))
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
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(playlistWithSongs.songs.map(DetailedSong::asMediaItem).shuffled())
                                    }
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .background(
                                        color = colorPalette.elevatedBackground,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                                    .size(20.dp)
                            )

                            Image(
                                painter = painterResource(R.drawable.play),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(playlistWithSongs.songs.map(DetailedSong::asMediaItem))
                                    }
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .background(
                                        color = colorPalette.elevatedBackground,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = playlistWithSongs.songs,
                    key = { _, song -> song.song.id },
                    contentType = { _, song -> song },
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(playlistWithSongs.songs.map(DetailedSong::asMediaItem), index)
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
                                                songId = playlistWithSongs.songs[index].song.id,
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
