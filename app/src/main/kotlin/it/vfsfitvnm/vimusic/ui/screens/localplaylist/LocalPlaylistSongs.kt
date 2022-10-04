package it.vfsfitvnm.vimusic.ui.screens.localplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.InPlaylistMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.completed
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.bodies.BrowseBody
import it.vfsfitvnm.youtubemusic.requests.playlistPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun LocalPlaylistSongs(
    playlistId: Long,
    onDelete: () -> Unit,
) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val rippleIndication = rememberRipple(bounded = true)

    val playlistWithSongs by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(PlaylistWithSongsSaver)
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

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

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
                    SecondaryTextButton(
                        text = "Enqueue",
                        isEnabled = playlistWithSongs?.songs?.isNotEmpty() == true,
                        onClick = {
                            playlistWithSongs?.songs
                                ?.map(DetailedSong::asMediaItem)
                                ?.let { mediaItems ->
                                    binder?.player?.enqueue(mediaItems)
                                }
                        }
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
                                                Innertube.playlistPage(BrowseBody(browseId = browseId))?.completed()
                                            }
                                        }?.getOrNull()?.let { remotePlaylist ->
                                            Database.clearPlaylist(playlistId)

                                            remotePlaylist.
                                                songsPage
                                                ?.items
                                                ?.map(Innertube.SongItem::asMediaItem)
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
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
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
                        .combinedClickable(
                            indication = rippleIndication,
                            interactionSource = remember { MutableInteractionSource() },
                            onLongClick = {
                                menuState.display {
                                    InPlaylistMediaItemMenu(
                                        playlistId = playlistId,
                                        positionInPlaylist = index,
                                        song = song
                                    )
                                }
                            },
                            onClick = {
                                playlistWithSongs?.songs?.map(DetailedSong::asMediaItem)
                                    ?.let { mediaItems ->
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(mediaItems, index)
                                    }
                            }
                        )
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
