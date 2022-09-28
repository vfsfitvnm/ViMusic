package it.vfsfitvnm.vimusic.ui.screens.playlist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.savers.YouTubePlaylistOrAlbumSaver
import it.vfsfitvnm.vimusic.savers.resultSaver
import it.vfsfitvnm.vimusic.transaction
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
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.produceSaveableOneShotState
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun PlaylistSongList(
    browseId: String,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    val playlistResult by produceSaveableOneShotState(
        initialValue = null,
        stateSaver = resultSaver(YouTubePlaylistOrAlbumSaver),
    ) {
        value = withContext(Dispatchers.IO) {
            YouTube.playlist(browseId)?.map { it.next() }
        }
    }

    val isImported by produceSaveableState(
        initialValue = null,
        stateSaver = autoSaver<Boolean?>(),
    ) {
        Database
            .isImportedPlaylist(browseId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    BoxWithConstraints {
        val thumbnailSizeDp = maxWidth - Dimensions.navigationRailWidth
        val thumbnailSizePx = (thumbnailSizeDp - 32.dp).px

        val songThumbnailSizeDp = Dimensions.thumbnails.song
        val songThumbnailSizePx = songThumbnailSizeDp.px

        playlistResult?.getOrNull()?.let { playlist ->
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
                        Header(title = playlist.title ?: "Unknown") {
                            SecondaryTextButton(
                                text = "Enqueue",
                                isEnabled = playlist.songs?.isNotEmpty() == true,
                                onClick = {
                                    playlist.songs?.map(YouTube.Item.Song::asMediaItem)?.let { mediaItems ->
                                        binder?.player?.enqueue(mediaItems)
                                    }
                                }
                            )

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                            )

                            Image(
                                painter = painterResource(
                                    if (isImported == true) R.drawable.bookmark else R.drawable.bookmark_outline
                                ),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.accent),
                                modifier = Modifier
                                    .clickable(enabled = isImported == false) {
                                        transaction {
                                            val playlistId =
                                                Database.insert(
                                                    Playlist(
                                                        name = playlist.title ?: "Unknown",
                                                        browseId = browseId
                                                    )
                                                )

                                            playlist.songs
                                                ?.map(YouTube.Item.Song::asMediaItem)
                                                ?.onEach(Database::insert)
                                                ?.mapIndexed { index, mediaItem ->
                                                    SongPlaylistMap(
                                                        songId = mediaItem.mediaId,
                                                        playlistId = playlistId,
                                                        position = index
                                                    )
                                                }?.let(Database::insertSongPlaylistMaps)
                                        }
                                    }
                                    .padding(all = 4.dp)
                                    .size(18.dp)
                            )

                            Image(
                                painter = painterResource(R.drawable.share_social),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .clickable {
                                        (playlist.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, url)
                                            }

                                            context.startActivity(Intent.createChooser(sendIntent, null))
                                        }
                                    }
                                    .padding(all = 4.dp)
                                    .size(18.dp)
                            )
                        }

                        AsyncImage(
                            model = playlist.thumbnail?.size(thumbnailSizePx),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(all = 16.dp)
                                .clip(thumbnailShape)
                                .size(thumbnailSizeDp)
                        )
                    }
                }

                itemsIndexed(items = playlist.songs ?: emptyList()) { index, song ->
                    SongItem(
                        title = song.info?.name,
                        authors = (song.authors ?: playlist.authors)?.joinToString("") { it.name ?: "" },
                        durationText = song.durationText,
                        onClick = {
                            playlist.songs?.map(YouTube.Item.Song::asMediaItem)?.let { mediaItems ->
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(mediaItems, index)
                            }
                        },
                        startContent = {
                            AsyncImage(
                                model = song.thumbnail?.size(songThumbnailSizePx),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(thumbnailShape)
                                    .size(Dimensions.thumbnails.song)
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
                isEnabled = playlist.songs?.isNotEmpty() == true,
                onClick = {
                    playlist.songs?.map(YouTube.Item.Song::asMediaItem)?.let { mediaItems ->
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(mediaItems.shuffled())
                    }
                }
            )
        } ?: playlistResult?.exceptionOrNull()?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
            ) {
                BasicText(
                    text = "An error has occurred.\nTap to retry",
                    style = typography.s.medium.secondary.center,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        } ?: Column(
            modifier = Modifier
                .padding(LocalPlayerAwarePaddingValues.current)
                .shimmer()
                .fillMaxSize()
        ) {
            HeaderPlaceholder()

            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
                    .clip(thumbnailShape)
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
