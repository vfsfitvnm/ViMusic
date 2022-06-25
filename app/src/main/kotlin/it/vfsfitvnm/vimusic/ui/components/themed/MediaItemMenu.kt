package it.vfsfitvnm.vimusic.ui.components.themed

import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.empty
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongInPlaylist
import it.vfsfitvnm.vimusic.models.SongWithInfo
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.screens.rememberArtistRoute
import it.vfsfitvnm.vimusic.ui.screens.rememberCreatePlaylistRoute
import it.vfsfitvnm.vimusic.ui.screens.rememberPlaylistOrAlbumRoute
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun InFavoritesMediaItemMenu(
    song: SongWithInfo,
    modifier: Modifier = Modifier,
    // https://issuetracker.google.com/issues/226410236
    onDismiss: () -> Unit = LocalMenuState.current.let { it::hide }
) {
    val coroutineScope = rememberCoroutineScope()

    NonQueuedMediaItemMenu(
        mediaItem = song.asMediaItem,
        onDismiss = onDismiss,
        onRemoveFromFavorites = {
            coroutineScope.launch(Dispatchers.IO) {
                Database.update(song.song.toggleLike())
            }
        },
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun InHistoryMediaItemMenu(
    song: SongWithInfo,
    modifier: Modifier = Modifier,
    // https://issuetracker.google.com/issues/226410236
    onDismiss: () -> Unit = LocalMenuState.current.let { it::hide }
) {
    val binder = LocalPlayerServiceBinder.current

    val coroutineScope = rememberCoroutineScope()

    var isDeletingFromDatabase by remember {
        mutableStateOf(false)
    }

    if (isDeletingFromDatabase) {
        ConfirmationDialog(
            text = "Do you really want to permanently delete this song? It will removed from any playlist as well.\nThis action is irreversible.",
            onDismiss = {
                isDeletingFromDatabase = false
            },
            onConfirm = {
                onDismiss()
                binder?.cache?.removeResource(song.song.id)
                coroutineScope.launch(Dispatchers.IO) {
                    Database.delete(song.song)
                }
            }
        )
    }

    NonQueuedMediaItemMenu(
        mediaItem = song.asMediaItem,
        onDismiss = onDismiss,
        onDeleteFromDatabase = {
            isDeletingFromDatabase = true
        },
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun InPlaylistMediaItemMenu(
    playlistId: Long,
    positionInPlaylist: Int,
    song: SongWithInfo,
    modifier: Modifier = Modifier,
    // https://issuetracker.google.com/issues/226410236
    onDismiss: () -> Unit = LocalMenuState.current.let { it::hide }
) {
    val coroutineScope = rememberCoroutineScope()

    NonQueuedMediaItemMenu(
        mediaItem = song.asMediaItem,
        onDismiss = onDismiss,
        onRemoveFromPlaylist = {
            coroutineScope.launch(Dispatchers.IO) {
                Database.internal.runInTransaction {
                    Database.delete(
                        SongInPlaylist(
                            songId = song.song.id,
                            playlistId = playlistId,
                            position = positionInPlaylist
                        )
                    )
                    Database.decrementSongPositions(
                        playlistId = playlistId,
                        fromPosition = positionInPlaylist + 1
                    )
                }
            }
        },
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun NonQueuedMediaItemMenu(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    // https://issuetracker.google.com/issues/226410236
    onDismiss: () -> Unit = LocalMenuState.current.let { it::hide },
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onDeleteFromDatabase: (() -> Unit)? = null,
    onRemoveFromFavorites: (() -> Unit)? = null,
) {
    val binder = LocalPlayerServiceBinder.current

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onStartRadio = {
            binder?.player?.forcePlay(mediaItem)
            binder?.startRadio(videoId = mediaItem.mediaId, playlistId = mediaItem.mediaMetadata.extras?.getString("playlistId"))
        },
        onPlaySingle = {
            binder?.player?.forcePlay(mediaItem)
        },
        onPlayNext = {
            binder?.player?.addNext(mediaItem)
        },
        onEnqueue = {
            binder?.player?.enqueue(mediaItem)
        },
        onRemoveFromPlaylist = onRemoveFromPlaylist,
        onDeleteFromDatabase = onDeleteFromDatabase,
        onRemoveFromFavorites = onRemoveFromFavorites,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun QueuedMediaItemMenu(
    mediaItem: MediaItem,
    indexInQueue: Int,
    modifier: Modifier = Modifier,
    // https://issuetracker.google.com/issues/226410236
    onDismiss: () -> Unit = LocalMenuState.current.let { it::hide },
    onGlobalRouteEmitted: (() -> Unit)? = null
) {
    val player = LocalPlayerServiceBinder.current?.player

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onRemoveFromQueue = {
            player?.removeMediaItem(indexInQueue)
        },
        onGlobalRouteEmitted = onGlobalRouteEmitted,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BaseMediaItemMenu(
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onStartRadio: (() -> Unit)? = null,
    onPlaySingle: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onDeleteFromDatabase: (() -> Unit)? = null,
    onRemoveFromFavorites: (() -> Unit)? = null,
    onGlobalRouteEmitted: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    MediaItemMenu(
        mediaItem = mediaItem,
        onDismiss = onDismiss,
        onStartRadio = onStartRadio,
        onPlayNext = onPlayNext,
        onPlaySingle = onPlaySingle,
        onEnqueue = onEnqueue,
        onAddToPlaylist = { playlist, position ->
            coroutineScope.launch(Dispatchers.IO) {
                val playlistId = Database.playlist(playlist.id)?.id ?: Database.insert(playlist)

                Database.insert(mediaItem)

                Database.insert(
                    SongInPlaylist(
                        songId = mediaItem.mediaId,
                        playlistId = playlistId,
                        position = position
                    )
                )
            }
        },
        onDeleteFromDatabase = onDeleteFromDatabase,
        onRemoveFromFavorites = onRemoveFromFavorites,
        onRemoveFromPlaylist = onRemoveFromPlaylist,
        onRemoveFromQueue = onRemoveFromQueue,
        onGoToAlbum = albumRoute::global,
        onGoToArtist = artistRoute::global,
        onShare = {
            context.shareAsYouTubeSong(mediaItem)
        },
        onGlobalRouteEmitted = onGlobalRouteEmitted,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun MediaItemMenu(
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onStartRadio: (() -> Unit)? = null,
    onPlaySingle: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onDeleteFromDatabase: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromFavorites: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onAddToPlaylist: ((Playlist, Int) -> Unit)? = null,
    onGoToAlbum: ((String) -> Unit)? = null,
    onGoToArtist: ((String) -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onGlobalRouteEmitted: (() -> Unit)? = null,
) {
    val playlistPreviews by remember {
        Database.playlistPreviews()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    val viewPlaylistsRoute = rememberCreatePlaylistRoute()

    Menu(
        modifier = modifier
    ) {
        RouteHandler(
            transitionSpec = {
                when (targetState.route) {
                    viewPlaylistsRoute -> slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)
                    else -> when (initialState.route) {
                        viewPlaylistsRoute -> slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
                        else -> empty
                    }
                }
            }
        ) {
            viewPlaylistsRoute {
                var isCreatingNewPlaylist by rememberSaveable {
                    mutableStateOf(false)
                }

                if (isCreatingNewPlaylist && onAddToPlaylist != null) {
                    TextFieldDialog(
                        hintText = "Enter the playlist name",
                        onDismiss = {
                            isCreatingNewPlaylist = false
                        },
                        onDone = { text ->
                            onDismiss()
                            onAddToPlaylist(Playlist(name = text), 0)
                        }
                    )
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        MenuBackButton(onClick = pop)

                        if (onAddToPlaylist != null) {
                            MenuIconButton(
                                icon = R.drawable.add,
                                onClick = {
                                    isCreatingNewPlaylist = true
                                }
                            )
                        }
                    }

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        playlistPreviews.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.list,
                                text = playlistPreview.playlist.name,
                                secondaryText = "${playlistPreview.songCount} songs",
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        playlistPreview.playlist,
                                        playlistPreview.songCount
                                    )
                                }
                            )
                        }
                    }
                }
            }

            host {
                Column(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        }
                ) {
                    MenuCloseButton(onClick = onDismiss)

                    onStartRadio?.let { onStartRadio ->
                        MenuEntry(
                            icon = R.drawable.radio,
                            text = "Start radio",
                            onClick = {
                                onDismiss()
                                onStartRadio()
                            }
                        )
                    }

                    onPlaySingle?.let { onPlaySingle ->
                        MenuEntry(
                            icon = R.drawable.play,
                            text = "Play single",
                            onClick = {
                                onDismiss()
                                onPlaySingle()
                            }
                        )
                    }

                    onPlayNext?.let { onPlayNext ->
                        MenuEntry(
                            icon = R.drawable.play,
                            text = "Play next",
                            onClick = {
                                onDismiss()
                                onPlayNext()
                            }
                        )
                    }

                    onEnqueue?.let { onEnqueue ->
                        MenuEntry(
                            icon = R.drawable.time,
                            text = "Enqueue",
                            onClick = {
                                onDismiss()
                                onEnqueue()
                            }
                        )
                    }

                    onRemoveFromQueue?.let { onRemoveFromQueue ->
                        MenuEntry(
                            icon = R.drawable.trash,
                            text = "Remove",
                            onClick = {
                                onDismiss()
                                onRemoveFromQueue()
                            }
                        )
                    }

                    onRemoveFromFavorites?.let { onRemoveFromFavorites ->
                        MenuEntry(
                            icon = R.drawable.heart_dislike,
                            text = "Dislike",
                            onClick = {
                                onDismiss()
                                onRemoveFromFavorites()
                            }
                        )
                    }

                    onRemoveFromPlaylist?.let { onRemoveFromPlaylist ->
                        MenuEntry(
                            icon = R.drawable.trash,
                            text = "Remove",
                            onClick = {
                                onDismiss()
                                onRemoveFromPlaylist()
                            }
                        )
                    }

                    if (onAddToPlaylist != null) {
                        MenuEntry(
                            icon = R.drawable.list,
                            text = "Add to playlist",
                            onClick = {
                                viewPlaylistsRoute()
                            }
                        )
                    }

                    onGoToAlbum?.let { onGoToAlbum ->
                        mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
                            MenuEntry(
                                icon = R.drawable.disc,
                                text = "Go to album",
                                onClick = {
                                    onDismiss()
                                    onGlobalRouteEmitted?.invoke()
                                    onGoToAlbum(albumId)
                                }
                            )
                        }
                    }

                    onGoToArtist?.let { onGoToArtist ->
                        mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")
                            ?.let { artistNames ->
                                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")
                                    ?.let { artistIds ->
                                        artistNames.zip(artistIds)
                                            .forEach { (authorName, authorId) ->
                                                if (authorId != null) {
                                                    MenuEntry(
                                                        icon = R.drawable.person,
                                                        text = "More of $authorName",
                                                        onClick = {
                                                            onDismiss()
                                                            onGlobalRouteEmitted?.invoke()
                                                            onGoToArtist(authorId)
                                                        }
                                                    )
                                                }
                                            }
                                    }
                            }
                    }

                    onShare?.let { onShare ->
                        MenuEntry(
                            icon = R.drawable.share_social,
                            text = "Share",
                            onClick = {
                                onDismiss()
                                onShare()
                            }
                        )
                    }

                    onDeleteFromDatabase?.let { onDeleteFromDatabase ->
                        MenuEntry(
                            icon = R.drawable.trash,
                            text = "Delete",
                            onClick = {
                                onDeleteFromDatabase()
                            }
                        )
                    }
                }
            }
        }
    }
}
