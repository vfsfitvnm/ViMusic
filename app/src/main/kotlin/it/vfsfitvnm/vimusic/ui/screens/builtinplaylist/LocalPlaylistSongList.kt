package it.vfsfitvnm.vimusic.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.savers.DetailedSongListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.InFavoritesMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistSongList(builtInPlaylist: BuiltInPlaylist) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val songs by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = DetailedSongListSaver
    ) {
        when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> Database
                .favorites()
                .flowOn(Dispatchers.IO)
            BuiltInPlaylist.Offline -> Database
                .songsWithContentLength()
                .flowOn(Dispatchers.IO)
                .map { songs ->
                songs.filter { song ->
                    song.contentLength?.let {
                        binder?.cache?.isCached(song.id, 0, song.contentLength)
                    } ?: false
                }
            }
        }.collect { value = it }
    }

    val thumbnailSize = Dimensions.thumbnails.song.px

    Box {
        LazyColumn(
            contentPadding = LocalPlayerAwarePaddingValues.current,
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.Favorites -> "Favorites"
                        BuiltInPlaylist.Offline -> "Offline"
                    }
                ) {
                    BasicText(
                        text = "Enqueue",
                        style = typography.xxs.medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = songs.isNotEmpty()) {
                                binder?.player?.enqueue(songs.map(DetailedSong::asMediaItem))
                            }
                            .background(colorPalette.background2)
                            .padding(all = 8.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
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
                            BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(song = song)
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(all = 16.dp)
                .padding(LocalPlayerAwarePaddingValues.current)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = songs.isNotEmpty()) {
                    binder?.stopRadio()
                    binder?.player?.forcePlayFromBeginning(songs.shuffled().map(DetailedSong::asMediaItem))
                }
                .background(colorPalette.background2)
                .size(62.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
            )
        }
    }
}
