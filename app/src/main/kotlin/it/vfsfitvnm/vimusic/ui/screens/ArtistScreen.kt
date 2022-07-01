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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


@ExperimentalAnimationApi
@Composable
fun ArtistScreen(
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
            val binder = LocalPlayerServiceBinder.current

            val density = LocalDensity.current
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current

            val artistResult by remember(browseId) {
                Database.artist(browseId).map { artist ->
                    artist?.takeIf {
                        artist.shufflePlaylistId != null
                    }?.let(Result.Companion::success) ?: YouTube.artist(browseId)
                        ?.map { youtubeArtist ->
                            Artist(
                                id = browseId,
                                name = youtubeArtist.name,
                                thumbnailUrl = youtubeArtist.thumbnail?.url,
                                info = youtubeArtist.description,
                                shuffleVideoId = youtubeArtist.shuffleEndpoint?.videoId,
                                shufflePlaylistId = youtubeArtist.shuffleEndpoint?.playlistId,
                                radioVideoId = youtubeArtist.radioEndpoint?.videoId,
                                radioPlaylistId = youtubeArtist.radioEndpoint?.playlistId,
                            ).also(Database::upsert)
                        }
                }.distinctUntilChanged()
            }.collectAsState(initial = null, context = Dispatchers.IO)

            val (thumbnailSizeDp, thumbnailSizePx) = remember {
                density.run {
                    192.dp to 192.dp.roundToPx()
                }
            }

            val songThumbnailSizePx = remember {
                density.run {
                    54.dp.roundToPx()
                }
            }

            val songs by remember(browseId) {
                Database.artistSongs(browseId)
            }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(bottom = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    artistResult?.getOrNull()?.let { artist ->
                        AsyncImage(
                            model = artist.thumbnailUrl?.thumbnail(thumbnailSizePx),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    query {
                                        runBlocking {
                                            Database
                                                .artist(browseId)
                                                .first()
                                                ?.copy(shufflePlaylistId = null)
                                                ?.let(Database::update)
                                        }
                                    }
                                }
                                .size(thumbnailSizeDp)
                        )

                        BasicText(
                            text = artist.name,
                            style = typography.l.semiBold,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        binder?.playRadio(
                                            NavigationEndpoint.Endpoint.Watch(
                                                videoId = artist.shuffleVideoId,
                                                playlistId = artist.shufflePlaylistId
                                            )
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
                                painter = painterResource(R.drawable.radio),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        binder?.playRadio(
                                            NavigationEndpoint.Endpoint.Watch(
                                                videoId = artist.radioVideoId,
                                                playlistId = artist.radioPlaylistId
                                            )
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
                    } ?: artistResult?.exceptionOrNull()?.let { throwable ->
                        LoadingOrError(
                            errorMessage = throwable.javaClass.canonicalName,
                            onRetry = {
                                query {
                                    runBlocking {
                                        Database.artist(browseId).first()?.let(Database::update)
                                    }
                                }
                            }
                        )
                    } ?: LoadingOrError()
                }

                item {
                    if (songs.isEmpty()) return@item

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .zIndex(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 32.dp)
                    ) {
                        BasicText(
                            text = "Local tracks",
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )

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
                    }
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.song.id },
                    contentType = { _, song -> song },
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = songThumbnailSizePx,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songs.map(DetailedSong::asMediaItem),
                                index
                            )
                        },
                        menuContent = {
                            InHistoryMediaItemMenu(song = song)
                        }
                    )
                }

                artistResult?.getOrNull()?.info?.let { description ->
                    item {
                        TextCard {
                            Title(text = "Information")
                            Text(text = description)
                        }
                    }
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
        onRetry = onRetry,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.darkGray, shape = CircleShape)
                .size(192.dp)
        )

        TextPlaceholder(
            modifier = Modifier
                .alpha(0.9f)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        )

        repeat(3) {
            TextPlaceholder(
                modifier = Modifier
                    .alpha(0.8f)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
