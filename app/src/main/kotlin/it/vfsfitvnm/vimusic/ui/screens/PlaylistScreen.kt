package it.vfsfitvnm.vimusic.ui.screens

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@ExperimentalAnimationApi
@Composable
fun PlaylistScreen(
    browseId: String,
) {
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
            val context = LocalContext.current
            val binder = LocalPlayerServiceBinder.current

            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val menuState = LocalMenuState.current

            val thumbnailSizePx = Dimensions.thumbnails.playlist.px
            val songThumbnailSizePx = Dimensions.thumbnails.song.px

            var playlist by remember {
                mutableStateOf<Result<YouTube.PlaylistOrAlbum>?>(null)
            }

            val onLoad = relaunchableEffect(Unit) {
                playlist = withContext(Dispatchers.IO) {
                    YouTube.playlistOrAlbum(browseId)
                }
            }

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
                                                onClick = {
                                                    menuState.hide()
                                                    playlist?.getOrNull()?.let { album ->
                                                        album.items
                                                            ?.mapNotNull { song ->
                                                                song.toMediaItem(browseId, album)
                                                            }
                                                            ?.let { mediaItems ->
                                                                binder?.player?.enqueue(
                                                                    mediaItems
                                                                )
                                                            }
                                                    }
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.list,
                                                text = "Import as playlist",
                                                onClick = {
                                                    menuState.hide()

                                                    playlist?.getOrNull()?.let { album ->
                                                        transaction {
                                                            val playlistId =
                                                                Database.insert(
                                                                    Playlist(
                                                                        name = album.title
                                                                            ?: "Unknown"
                                                                    )
                                                                )

                                                            album.items?.forEachIndexed { index, song ->
                                                                song
                                                                    .toMediaItem(browseId, album)
                                                                    ?.let { mediaItem ->
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

                                            MenuEntry(
                                                icon = R.drawable.share_social,
                                                text = "Share",
                                                onClick = {
                                                    menuState.hide()

                                                    (playlist?.getOrNull()?.url
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
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
                    playlist?.getOrNull()?.let { playlist ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .padding(bottom = 16.dp)
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
                                        text = buildString {
                                            val authors =
                                                playlist.authors?.joinToString("") { it.name }
                                            append(authors)
                                            if (authors?.isNotEmpty() == true && playlist.year != null) {
                                                append(" • ")
                                            }
                                            append(playlist.year)
                                        },
                                        style = typography.xs.secondary.semiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(horizontal = 16.dp)
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
                                                playlist.items
                                                    ?.mapNotNull { song ->
                                                        song.toMediaItem(browseId, playlist)
                                                    }
                                                    ?.let { mediaItems ->
                                                        binder?.player?.forcePlayFromBeginning(
                                                            mediaItems
                                                        )
                                                    }
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
    val colorPalette = LocalColorPalette.current

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
                    .background(color = colorPalette.darkGray, shape = ThumbnailRoundness.shape)
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
                            .background(color = colorPalette.darkGray, shape = CircleShape)
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
