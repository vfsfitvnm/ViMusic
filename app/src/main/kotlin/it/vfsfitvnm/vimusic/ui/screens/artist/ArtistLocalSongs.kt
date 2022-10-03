package it.vfsfitvnm.vimusic.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.savers.DetailedSongListSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.ShimmerHost
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.ui.views.SongItemPlaceholder
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@ExperimentalAnimationApi
@Composable
fun ArtistLocalSongs(
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable ColumnScope.() -> Unit,
) {
    val binder = LocalPlayerServiceBinder.current
    val (colorPalette) = LocalAppearance.current

    val songs by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(DetailedSongListSaver)
    ) {
        Database
            .artistSongs(browseId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val songThumbnailSizePx = Dimensions.thumbnails.song.px

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
                Column {
                    headerContent {
                        SecondaryTextButton(
                            text = "Enqueue",
                            isEnabled = !songs.isNullOrEmpty(),
                            onClick = {
                                binder?.player?.enqueue(songs!!.map(DetailedSong::asMediaItem))
                            }
                        )
                    }

                    thumbnailContent()
                }
            }

            songs?.let { songs ->
                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    SongItem(
                        song = song,
                        thumbnailSizePx = songThumbnailSizePx,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                songs.map(DetailedSong::asMediaItem),
                                index
                            )
                        },
                        menuContent = {
                            NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
                        }
                    )
                }
            } ?: item(key = "loading") {
                ShimmerHost {
                    repeat(4) {
                        SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                    }
                }
            }
        }

        PrimaryButton(
            iconId = R.drawable.shuffle,
            isEnabled = !songs.isNullOrEmpty(),
            onClick = {
                binder?.stopRadio()
                binder?.player?.forcePlayFromBeginning(
                    songs!!.shuffled().map(DetailedSong::asMediaItem)
                )
            }
        )
    }
}
