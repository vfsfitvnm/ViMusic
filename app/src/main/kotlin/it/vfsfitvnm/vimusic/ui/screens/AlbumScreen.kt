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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.models.SongAlbumMap
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


@ExperimentalAnimationApi
@Composable
fun AlbumScreen(
    browseId: String
) {
    val lazyListState = rememberLazyListState()

    val albumResult by remember(browseId) {
        Database.album(browseId).map { album ->
            album?.takeIf {
                album.thumbnailUrl != null
            }?.let(Result.Companion::success) ?: YouTube.playlistOrAlbum(browseId)
                ?.map { youtubeAlbum ->
                    Album(
                        id = browseId,
                        title = youtubeAlbum.title,
                        thumbnailUrl = youtubeAlbum.thumbnail?.url,
                        year = youtubeAlbum.year,
                        authorsText = youtubeAlbum.authors?.joinToString("") { it.name },
                        shareUrl = youtubeAlbum.url
                    ).also(Database::upsert).also {
                        youtubeAlbum.items?.forEachIndexed { position, albumItem ->
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
        }.distinctUntilChanged()
    }.collectAsState(initial = null, context = Dispatchers.IO)

    val songs by remember(browseId) {
        Database.albumSongs(browseId)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

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
                                                    binder?.player?.enqueue(
                                                        songs.map(DetailedSong::asMediaItem)
                                                    )
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.share_social,
                                                text = "Share",
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
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .size(24.dp)
                        )
                    }
                }

                item {
                    albumResult?.getOrNull()?.let { album ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .padding(bottom = 16.dp)
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
                                    .fillMaxSize()
                            ) {
                                Column {
                                    BasicText(
                                        text = album.title ?: "Unknown",
                                        style = typography.m.semiBold
                                    )

                                    BasicText(
                                        text = buildString {
                                            append(album.authorsText)
                                            if (album.authorsText?.isNotEmpty() == true && album.year != null) {
                                                append(" • ")
                                            }
                                            append(album.year)
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
                                                binder?.player?.forcePlayFromBeginning(
                                                    songs.shuffled().map(DetailedSong::asMediaItem)
                                                )
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
                                                binder?.player?.forcePlayFromBeginning(
                                                    songs.map(DetailedSong::asMediaItem)
                                                )
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
                    } ?: albumResult?.exceptionOrNull()?.let { throwable ->
                        LoadingOrError(errorMessage = throwable.javaClass.canonicalName)
                    } ?: LoadingOrError()
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.song.id },
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        title = song.song.title,
                        authors = song.song.artistsText ?: albumResult?.getOrNull()?.authorsText,
                        durationText = song.song.durationText,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(songs.map(DetailedSong::asMediaItem), index)
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
                    .background(color = LocalColorPalette.current.darkGray, shape = ThumbnailRoundness.shape)
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
