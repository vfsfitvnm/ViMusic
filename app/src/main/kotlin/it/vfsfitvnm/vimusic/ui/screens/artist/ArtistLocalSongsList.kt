package it.vfsfitvnm.vimusic.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.savers.DetailedSongListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@ExperimentalAnimationApi
@Composable
fun ArtistLocalSongsList(
    browseId: String,
    artist: Artist?,
    isLoading: Boolean,
    isError: Boolean,
    bookmarkIconContent: @Composable () -> Unit,
    shareIconContent: @Composable () -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val songs by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = DetailedSongListSaver
    ) {
        Database
            .artistSongs(browseId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val songThumbnailSizePx = Dimensions.thumbnails.song.px

    BoxWithConstraints {
        val thumbnailSizeDp = maxWidth - Dimensions.navigationRailWidth
        val thumbnailSizePx = (thumbnailSizeDp - 32.dp).px

        when {
            artist != null -> {
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
                            Header(title = artist.name ?: "Unknown") {
                                SecondaryTextButton(
                                    text = "Enqueue",
                                    isEnabled = songs.isNotEmpty(),
                                    onClick = {
                                        binder?.player?.enqueue(songs.map(DetailedSong::asMediaItem))
                                    }
                                )

                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                )

                                bookmarkIconContent()
                                shareIconContent()
                            }

                            AsyncImage(
                                model = artist.thumbnailUrl?.thumbnail(thumbnailSizePx),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(all = 16.dp)
                                    .clip(CircleShape)
                                    .size(thumbnailSizeDp)
                            )
                        }
                    }

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
                }

                PrimaryButton(
                    iconId = R.drawable.shuffle,
                    isEnabled = songs.isNotEmpty(),
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs.shuffled().map(DetailedSong::asMediaItem)
                        )
                    }
                )
            }
            isError -> Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
            ) {
                BasicText(
                    text = "An error has occurred.",
                    style = typography.s.secondary.center,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
            isLoading -> ShimmerHost {
                HeaderPlaceholder()

                Spacer(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                        .clip(CircleShape)
                        .size(thumbnailSizeDp)
                        .background(colorPalette.shimmer)
                )

                repeat(3) { index ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .alpha(1f - index * 0.25f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = Dimensions.itemsVerticalPadding)
                            .height(Dimensions.thumbnails.song)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                                .size(Dimensions.thumbnails.song)
                        )

                        Column {
                            TextPlaceholder()
                            TextPlaceholder()
                        }
                    }
                }
            }

        }
    }
}
