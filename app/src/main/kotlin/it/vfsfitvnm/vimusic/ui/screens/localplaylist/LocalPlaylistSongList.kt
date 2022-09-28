package it.vfsfitvnm.vimusic.ui.screens.localplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.reordering.ReorderingLazyColumn
import it.vfsfitvnm.reordering.animateItemPlacement
import it.vfsfitvnm.reordering.draggedItem
import it.vfsfitvnm.reordering.rememberReorderingState
import it.vfsfitvnm.reordering.reorder
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.savers.PlaylistWithSongsSaver
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.InPlaylistMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun LocalPlaylistSongList(
    playlistId: Long,
    onDelete: () -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val playlistWithSongs by produceSaveableState(
        initialValue = null,
        stateSaver = PlaylistWithSongsSaver
    ) {
        Database
            .playlistWithSongs(playlistId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val lazyListState = rememberLazyListState()

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = playlistWithSongs?.songs ?: emptyList<Any>(),
        onDragEnd = { fromIndex, toIndex ->
            query {
                Database.move(playlistId, fromIndex, toIndex)
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
            initialTextInput = playlistWithSongs?.playlist?.name ?: "",
            onDismiss = { isRenaming = false },
            onDone = { text ->
                query {
                    playlistWithSongs?.playlist?.copy(name = text)?.let(Database::update)
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
                    playlistWithSongs?.playlist?.let(Database::delete)
                }
                onDelete()
            }
        )
    }

    val thumbnailSize = Dimensions.thumbnails.song.px

    Box {
        ReorderingLazyColumn(
            reorderingState = reorderingState,
            contentPadding = LocalPlayerAwarePaddingValues.current,
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(title = playlistWithSongs?.playlist?.name ?: "Unknown") {
                    BasicText(
                        text = "Enqueue",
                        style = typography.xxs.medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = playlistWithSongs?.songs?.isNotEmpty() == true) {
                                playlistWithSongs?.songs
                                    ?.map(DetailedSong::asMediaItem)
                                    ?.let { mediaItems ->
                                        binder?.player?.enqueue(mediaItems)
                                    }
                            }
                            .background(colorPalette.background2)
                            .padding(all = 8.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    playlistWithSongs?.playlist?.browseId?.let { browseId ->
                        Image(
                            painter = painterResource(R.drawable.sync),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    transaction {
                                        runBlocking(Dispatchers.IO) {
                                            withContext(Dispatchers.IO) {
                                                YouTube.playlist(browseId)?.map {
                                                    it.next()
                                                }?.map { playlist ->
                                                    playlist.copy(songs = playlist.songs?.filter { it.info.endpoint != null })
                                                }
                                            }
                                        }?.getOrNull()?.let { remotePlaylist ->
                                            Database.clearPlaylist(playlistId)

                                            remotePlaylist.songs
                                                ?.map(YouTube.Item.Song::asMediaItem)
                                                ?.onEach(Database::insert)
                                                ?.mapIndexed { position, mediaItem ->
                                                    SongPlaylistMap(
                                                        songId = mediaItem.mediaId,
                                                        playlistId = playlistId,
                                                        position = position
                                                    )
                                                }?.let(Database::insertSongPlaylistMaps)
                                        }
                                    }
                                }
                                .padding(all = 4.dp)
                                .size(18.dp)
                        )
                    }

                    Image(
                        painter = painterResource(R.drawable.pencil),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable { isRenaming = true }
                            .padding(all = 4.dp)
                            .size(18.dp)
                    )


                    Image(
                        painter = painterResource(R.drawable.trash),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable { isDeleting = true }
                            .padding(all = 4.dp)
                            .size(18.dp)
                    )
                }
            }

            itemsIndexed(
                items = playlistWithSongs?.songs ?: emptyList(),
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSize = thumbnailSize,
                    onClick = {
                        playlistWithSongs?.songs?.map(DetailedSong::asMediaItem)
                            ?.let { mediaItems ->
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(mediaItems, index)
                            }
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

        PrimaryButton(
            iconId = R.drawable.shuffle,
            isEnabled = playlistWithSongs?.songs?.isNotEmpty() == true,
            onClick = {
                playlistWithSongs?.songs
                    ?.shuffled()
                    ?.map(DetailedSong::asMediaItem)
                    ?.let { mediaItems ->
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(mediaItems)
                    }
            }
        )
    }
}
