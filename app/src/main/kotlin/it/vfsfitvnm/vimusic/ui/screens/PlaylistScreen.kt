package it.vfsfitvnm.vimusic.ui.screens

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.transaction
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
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.add
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.relaunchableEffect
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toMediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun PlaylistScreen(browseId: String) {
    val lazyListState = rememberLazyListState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val context = LocalContext.current
            val binder = LocalPlayerServiceBinder.current

            val (colorPalette, typography) = LocalAppearance.current
            val menuState = LocalMenuState.current

            val thumbnailSizePx = Dimensions.thumbnails.playlist.px
            val songThumbnailSizePx = Dimensions.thumbnails.song.px

            var playlist by remember {
                mutableStateOf<Result<YouTube.PlaylistOrAlbum>?>(null)
            }

            val onLoad = relaunchableEffect(Unit) {
                playlist = withContext(Dispatchers.IO) {
                    YouTube.playlistOrAlbum(browseId)?.map {
                        it.next()
                    }?.map { playlist ->
                        playlist.copy(items = playlist.items?.filter { it.info.endpoint != null })
                    }
                }
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
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
                    playlist?.getOrNull()?.let { playlist ->
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                AsyncImage(
                                    model = playlist.thumbnail?.size(thumbnailSizePx),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(ThumbnailRoundness.shape)
                                        .size(Dimensions.thumbnails.playlist)
                                )

                                Column(
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Column {
                                        BasicText(
                                            text = playlist.title ?: "Unknown",
                                            style = typography.m.semiBold
                                        )

                                        BasicText(
                                            text = playlist.authors?.joinToString("") { it.name }
                                                ?: "",
                                            style = typography.xs.secondary.semiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    playlist.year?.let { year ->
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
                                        .clickable {
                                            binder?.stopRadio()
                                            playlist.items
                                                ?.shuffled()
                                                ?.mapNotNull { song ->
                                                    song.toMediaItem(browseId, playlist)
                                                }
                                                ?.let { mediaItems ->
                                                    binder?.player?.forcePlayFromBeginning(
                                                        mediaItems
                                                    )
                                                }
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
                                                            playlist.items
                                                                ?.mapNotNull { song ->
                                                                    song.toMediaItem(
                                                                        browseId,
                                                                        playlist
                                                                    )
                                                                }
                                                                ?.let { mediaItems ->
                                                                    binder?.player?.enqueue(
                                                                        mediaItems
                                                                    )
                                                                }
                                                        }
                                                    )

                                                    MenuEntry(
                                                        icon = R.drawable.playlist,
                                                        text = stringResource(R.string.import_playlist),
                                                        onClick = {
                                                            menuState.hide()
                                                            transaction {
                                                                val playlistId =
                                                                    Database.insert(
                                                                        Playlist(
                                                                            name = playlist.title
                                                                                ?: "Unknown"
                                                                        )
                                                                    )

                                                                playlist.items?.forEachIndexed { index, song ->
                                                                    song
                                                                        .toMediaItem(
                                                                            browseId,
                                                                            playlist
                                                                        )
                                                                        ?.let { mediaItem ->
                                                                            Database.insert(
                                                                                mediaItem
                                                                            )

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
                                                    )

                                                    MenuEntry(
                                                        icon = R.drawable.share_social,
                                                        text = stringResource(R.string.share),
                                                        onClick = {
                                                            menuState.hide()

                                                            (playlist.url
                                                                ?: "https://music.youtube.com/playlist?list=${
                                                                    browseId.removePrefix(
                                                                        "VL"
                                                                    )
                                                                }").let { url ->
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
                                                }
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                    } ?: playlist?.exceptionOrNull()?.let { throwable ->
                        LoadingOrError(
                            errorMessage = throwable.javaClass.canonicalName,
                            onRetry = onLoad
                        )
                    } ?: LoadingOrError()
                }

                itemsIndexed(
                    items = playlist?.getOrNull()?.items ?: emptyList(),
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        title = song.info.name,
                        authors = (song.authors
                            ?: playlist?.getOrNull()?.authors)?.joinToString("") { it.name },
                        durationText = song.durationText,
                        mediaItem = MediaItem.EMPTY,
                        swipeShow = true,
                        onClick = {
                            binder?.stopRadio()
                            playlist?.getOrNull()?.items?.mapNotNull { song ->
                                song.toMediaItem(browseId, playlist?.getOrNull()!!)
                            }?.let { mediaItems ->
                                binder?.player?.forcePlayAtIndex(mediaItems, index)
                            }
                        },
                        startContent = {
                            if (song.thumbnail == null) {
                                BasicText(
                                    text = "${index + 1}",
                                    style = typography.xs.secondary.bold.center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .width(36.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = song.thumbnail!!.size(songThumbnailSizePx),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(ThumbnailRoundness.shape)
                                        .size(Dimensions.thumbnails.song)
                                )
                            }
                        },
                        menuContent = {
                            NonQueuedMediaItemMenu(
                                mediaItem = song.toMediaItem(
                                    browseId,
                                    playlist?.getOrNull()!!
                                )
                                    ?: return@SongItem,
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
                .padding(bottom = 16.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .background(color = colorPalette.shimmer, shape = ThumbnailRoundness.shape)
                    .size(Dimensions.thumbnails.playlist)
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

        repeat(3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .alpha(0.6f - it * 0.1f)
                    .height(Dimensions.thumbnails.song)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color = Color.Black, shape = CircleShape)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
