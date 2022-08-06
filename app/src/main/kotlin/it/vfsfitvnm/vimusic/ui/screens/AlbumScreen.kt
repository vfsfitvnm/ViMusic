package it.vfsfitvnm.vimusic.ui.screens

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.vimusic.utils.toMediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@ExperimentalAnimationApi
@Composable
fun AlbumScreen(browseId: String) {
    val lazyListState = rememberLazyListState()

    val albumResult by remember(browseId) {
        Database.album(browseId).map { album ->
            album
                ?.takeIf { album.timestamp != null }
                ?.let(Result.Companion::success)
                ?: fetchAlbum(browseId)
        }.distinctUntilChanged()
    }.collectAsState(initial = null, context = Dispatchers.IO)

    val songs by remember(browseId) {
        Database.albumSongs(browseId)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val context = LocalContext.current
            val binder = LocalPlayerServiceBinder.current

            val (colorPalette, typography) = LocalAppearance.current
            val menuState = LocalMenuState.current

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(bottom = 72.dp),
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
                    }
                }

                item {
                    albumResult?.getOrNull()?.let { album ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            AsyncImage(
                                model = album.thumbnailUrl?.thumbnail(Dimensions.thumbnails.album.px),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(ThumbnailRoundness.shape)
                                    .size(Dimensions.thumbnails.album)
                            )

                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                BasicText(
                                    text = album.title ?: stringResource(R.string.unknown),
                                    style = typography.m.semiBold
                                )

                                BasicText(
                                    text = album.authorsText ?: "",
                                    style = typography.xs.secondary.semiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                album.year?.let { year ->
                                    BasicText(
                                        text = year,
                                        style = typography.xs.secondary,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    } ?: albumResult?.exceptionOrNull()?.let { throwable ->
                        LoadingOrError(errorMessage = throwable.javaClass.canonicalName)
                    } ?: LoadingOrError()
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
                                .clickable(enabled = songs.isNotEmpty()) {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayFromBeginning(
                                        songs
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
                                                onClick = {
                                                    menuState.hide()
                                                    binder?.player?.enqueue(
                                                        songs.map(DetailedSong::asMediaItem)
                                                    )
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.playlist,
                                                text = stringResource(R.string.import_as_playlist),
                                                onClick = {
                                                    menuState.hide()

                                                    albumResult
                                                        ?.getOrNull()
                                                        ?.let { album ->
                                                            query {
                                                                val playlistId =
                                                                    Database.insert(
                                                                        Playlist(
                                                                            name = album.title
                                                                                ?: "Unknown"
                                                                        )
                                                                    )

                                                                songs.forEachIndexed { index, song ->
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.id,
                                                                            playlistId = playlistId,
                                                                            position = index
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.share_social,
                                                text = stringResource(R.string.share),
                                                onClick = {
                                                    menuState.hide()

                                                    albumResult?.getOrNull()?.shareUrl?.let { url ->
                                                        val sendIntent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            type = "text/plain"
                                                            putExtra(Intent.EXTRA_TEXT, url)
                                                        }

                                                        context.startActivity(
                                                            Intent.createChooser(
                                                                sendIntent,
                                                                null
                                                            )
                                                        )
                                                    }
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.download,
                                                text = stringResource(R.string.refetch),
                                                secondaryText = albumResult?.getOrNull()?.timestamp?.let { timestamp ->
                                                    stringResource(R.string.last_update)+ DateFormat
                                                        .getDateTimeInstance()
                                                        .format(Date(timestamp))
                                                },
                                                isEnabled = albumResult?.getOrNull() != null,
                                                onClick = {
                                                    menuState.hide()

                                                    query {
                                                        albumResult
                                                            ?.getOrNull()
                                                            ?.let(Database::delete)
                                                        runBlocking(Dispatchers.IO) {
                                                            fetchAlbum(browseId)
                                                        }
                                                    }
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
                    items = songs,
                    key = { _, song -> song.id },
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        title = song.title,
                        authors = song.artistsText ?: albumResult?.getOrNull()?.authorsText,
                        durationText = song.durationText,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songs.map(DetailedSong::asMediaItem),
                                index
                            )
                        },
                        startContent = {
                            BasicText(
                                text = "${index + 1}",
                                style = typography.xs.secondary.bold.center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(36.dp)
                            )
                        },
                        menuContent = {
                            NonQueuedMediaItemMenu(
                                mediaItem = song.asMediaItem,
                                onDismiss = menuState::hide,
                            )
                        }
                    )
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
    val (colorPalette) = LocalAppearance.current

    LoadingOrError(
        errorMessage = errorMessage,
        onRetry = onRetry
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .background(color = colorPalette.darkGray, shape = ThumbnailRoundness.shape)
                    .size(Dimensions.thumbnails.album)
            )

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                Column {
                    TextPlaceholder()

                    TextPlaceholder(
                        modifier = Modifier
                            .alpha(0.7f)
                    )
                }
            }
        }
    }
}

private suspend fun fetchAlbum(browseId: String): Result<Album>? {
    return YouTube.playlistOrAlbum(browseId)
        ?.map { youtubeAlbum ->
            Album(
                id = browseId,
                title = youtubeAlbum.title,
                thumbnailUrl = youtubeAlbum.thumbnail?.url,
                year = youtubeAlbum.year,
                authorsText = youtubeAlbum.authors?.joinToString("") { it.name },
                shareUrl = youtubeAlbum.url,
                timestamp = System.currentTimeMillis()
            ).also(Database::upsert).also {
                youtubeAlbum.withAudioSources().items?.forEachIndexed { position, albumItem ->
                    albumItem.toMediaItem(browseId, youtubeAlbum)?.let { mediaItem ->
                        Database.insert(mediaItem)
                        Database.upsert(
                            SongAlbumMap(
                                songId = mediaItem.mediaId,
                                albumId = browseId,
                                position = position
                            )
                        )
                    }
                }
            }
        }
}
