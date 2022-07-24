package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.InFavoritesMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistScreen(builtInPlaylist: BuiltInPlaylist) {
    val lazyListState = rememberLazyListState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val menuState = LocalMenuState.current

            val binder = LocalPlayerServiceBinder.current
            val (colorPalette, typography) = LocalAppearance.current

            val thumbnailSize = Dimensions.thumbnails.song.px

            val songs by remember(binder?.cache, builtInPlaylist) {
                when (builtInPlaylist) {
                    BuiltInPlaylist.Favorites -> Database.favorites()
                    BuiltInPlaylist.Cached -> Database.songsWithContentLength().map { songs ->
                        songs.filter { song ->
                            song.contentLength?.let {
                                binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                            } ?: false
                        }
                    }
                }
            }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

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
                                                isEnabled = songs.isNotEmpty(),
                                                onClick = {
                                                    menuState.hide()
                                                    binder?.player?.enqueue(songs.map(DetailedSong::asMediaItem))
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
                                text = when (builtInPlaylist) {
                                    BuiltInPlaylist.Favorites -> "Favorites"
                                    BuiltInPlaylist.Cached -> "Offline"
                                },
                                style = typography.m.semiBold
                            )

                            BasicText(
                                text = "${songs.size} songs",
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
                                        binder?.player?.forcePlayFromBeginning(
                                            songs
                                                .map(DetailedSong::asMediaItem)
                                                .shuffled()
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
                                            songs.map(
                                                DetailedSong::asMediaItem
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
                    }
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.song.id },
                    contentType = { _, song -> song },
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSize = thumbnailSize,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songs.map(DetailedSong::asMediaItem),
                                index
                            )
                        },
                        menuContent = {
                            when (builtInPlaylist) {
                                BuiltInPlaylist.Favorites -> InFavoritesMediaItemMenu(song = song)
                                BuiltInPlaylist.Cached -> NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
                            }
                        }
                    )
                }
            }
        }
    }
}
