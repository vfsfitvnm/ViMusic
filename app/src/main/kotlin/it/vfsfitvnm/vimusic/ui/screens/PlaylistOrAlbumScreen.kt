package it.vfsfitvnm.vimusic.ui.screens

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongInPlaylist
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.OutcomeItem
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@ExperimentalAnimationApi
@Composable
fun PlaylistOrAlbumScreen(
    browseId: String,
) {
    val scrollState = rememberScrollState()

    var playlistOrAlbum by remember {
        mutableStateOf<Outcome<YouTube.PlaylistOrAlbum>>(Outcome.Loading)
    }

    val onLoad = relaunchableEffect(Unit) {
        playlistOrAlbum = withContext(Dispatchers.IO) {
            YouTube.playlistOrAlbum(browseId)
        }
    }

    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
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
            val density = LocalDensity.current
            val player = LocalYoutubePlayer.current
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val menuState = LocalMenuState.current

            val (thumbnailSizeDp, thumbnailSizePx) = remember {
                density.run {
                    128.dp to 128.dp.roundToPx()
                }
            }

            val (songThumbnailSizeDp, songThumbnailSizePx) = remember {
                density.run {
                    54.dp to 54.dp.roundToPx()
                }
            }

            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp)
            ) {
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
                                            enabled = player?.playbackState == Player.STATE_READY,
                                            onClick = {
                                                menuState.hide()
                                                playlistOrAlbum.valueOrNull?.let { album ->
                                                    album.items
                                                        ?.mapNotNull { song ->
                                                            song.toMediaItem(browseId, album)
                                                        }
                                                        ?.let { mediaItems ->
                                                            player?.mediaController?.enqueue(
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

                                                playlistOrAlbum.valueOrNull?.let { album ->
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        Database.internal.runInTransaction {
                                                            val playlistId =
                                                                Database.insert(Playlist(name = album.title ?: "Unknown"))

                                                            album.items?.forEachIndexed { index, song ->
                                                                song
                                                                    .toMediaItem(browseId, album)
                                                                    ?.let { mediaItem ->
                                                                        if (Database.song(mediaItem.mediaId) == null) {
                                                                            Database.insert(
                                                                                mediaItem
                                                                            )
                                                                        }

                                                                        Database.insert(
                                                                            SongInPlaylist(
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
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.share_social,
                                            text = "Share",
                                            onClick = {
                                                menuState.hide()

                                                playlistOrAlbum.valueOrNull?.url?.let { url ->
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, url)
                                                    }

                                                    context.startActivity(Intent.createChooser(sendIntent, null))
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

                OutcomeItem(
                    outcome = playlistOrAlbum,
                    onRetry = onLoad,
                    onLoading = {
                        Loading()
                    }
                ) { playlistOrAlbum ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        AsyncImage(
                            model = playlistOrAlbum.thumbnail?.size(thumbnailSizePx),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .clip(ThumbnailRoundness.shape)
                                .size(thumbnailSizeDp)
                        )

                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Column {
                                BasicText(
                                    text = playlistOrAlbum.title ?: "Unknown",
                                    style = typography.m.semiBold
                                )

                                BasicText(
                                    text = buildString {
                                        val authors = playlistOrAlbum.authors?.joinToString("") { it.name }
                                        append(authors)
                                        if (authors?.isNotEmpty() == true && playlistOrAlbum.year != null) {
                                            append(" • ")
                                        }
                                        append(playlistOrAlbum.year)
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
                                            YoutubePlayer.Radio.reset()
                                            playlistOrAlbum.items
                                                ?.shuffled()
                                                ?.mapNotNull { song ->
                                                    song.toMediaItem(browseId, playlistOrAlbum)
                                                }?.let { mediaItems ->
                                                    player?.mediaController?.forcePlayFromBeginning(mediaItems)
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
                                            YoutubePlayer.Radio.reset()

                                            playlistOrAlbum.items?.mapNotNull { song ->
                                                song.toMediaItem(browseId, playlistOrAlbum)
                                            }?.let { mediaItems ->
                                                player?.mediaController?.forcePlayFromBeginning(mediaItems)
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

                    playlistOrAlbum.items?.forEachIndexed { index, song ->
                        SongItem(
                            title = song.info.name,
                            authors = (song.authors ?: playlistOrAlbum.authors)?.joinToString("") { it.name },
                            durationText = song.durationText,
                            onClick = {
                                YoutubePlayer.Radio.reset()

                                playlistOrAlbum.items?.mapNotNull { song ->
                                    song.toMediaItem(browseId, playlistOrAlbum)
                                }?.let { mediaItems ->
                                    player?.mediaController?.forcePlayAtIndex(mediaItems, index)
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
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .clip(ThumbnailRoundness.shape)
                                            .size(songThumbnailSizeDp)
                                    )
                                }
                            },
                            menuContent = {
                                NonQueuedMediaItemMenu(
                                    mediaItem = song.toMediaItem(browseId, playlistOrAlbum)
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
}

@Composable
private fun Loading() {
    val colorPalette = LocalColorPalette.current

    Column(
        modifier = Modifier
            .shimmer()
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
                    .size(128.dp)
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
                    .height(54.dp)
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
