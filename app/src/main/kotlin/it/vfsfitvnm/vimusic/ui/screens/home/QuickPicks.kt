package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.savers.DetailedSongSaver
import it.vfsfitvnm.vimusic.savers.YouTubeRelatedSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.savers.resultSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.SmallSongItem
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.produceSaveableOneShotState
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn

@ExperimentalAnimationApi
@Composable
fun QuickPicks(
    onAlbumClick: (String) -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val trending by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(DetailedSongSaver),
    ) {
        Database.trending()
            .flowOn(Dispatchers.IO)
            .filterNotNull()
            .distinctUntilChanged()
            .collect { value = it }
    }

    val relatedResult by produceSaveableOneShotState(
        initialValue = null,
        stateSaver = resultSaver(nullableSaver(YouTubeRelatedSaver)),
        trending?.id
    ) {
        println("trendingVideoId: ${trending?.id}")
        trending?.id?.let { trendingVideoId ->
            value = YouTube.related(trendingVideoId)?.map { related ->
                related?.copy(
                    albums = related.albums?.map { album ->
                        album.copy(
                            authors = trending?.artists?.map { info ->
                                YouTube.Info(
                                    name = info.name,
                                    endpoint = NavigationEndpoint.Endpoint.Browse(
                                        browseId = info.id,
                                        params = null,
                                        browseEndpointContextSupportedConfigs = null
                                    )
                                )
                            }
                        )
                    }
                )
            }
        }
    }

    val songThumbnailSizePx = Dimensions.thumbnails.song.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
//    val itemInHorizontalGridWidth = (LocalConfiguration.current.screenWidthDp.dp) * 0.8f

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
            Header(title = "Quick picks")
        }

        trending?.let { song ->
            item(key = song.id) {
                SongItem(
                    song = song,
                    thumbnailSizePx = songThumbnailSizePx,
                    onClick = {
                        val mediaItem = song.asMediaItem
                        binder?.stopRadio()
                        binder?.player?.forcePlay(mediaItem)
                        binder?.setupRadio(
                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                        )
                    },
                    menuContent = {
                        NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
                    }
                )
            }
        }

        relatedResult?.getOrNull()?.let { related ->
            items(
                items = related.songs?.take(6) ?: emptyList(),
                key = YouTube.Item::key
            ) { song ->
                SmallSongItem(
                    song = song,
                    thumbnailSizePx = songThumbnailSizePx,
                    onClick = {
                        val mediaItem = song.asMediaItem
                        binder?.stopRadio()
                        binder?.player?.forcePlay(mediaItem)
                        binder?.setupRadio(
                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                        )
                    },
                )
            }

            item(
                key = "albums",
                contentType = "LazyRow"
            ) {
                LazyRow {
                    items(
                        items = related.albums ?: emptyList(),
                        key = YouTube.Item::key
                    ) { album ->
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = albumThumbnailSizePx,
                            thumbnailSizeDp = albumThumbnailSizeDp,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onAlbumClick(album.key) }
                                )
                                .fillMaxWidth()
                        )
                    }
                }
            }

            items(
                items = related.songs?.drop(6) ?: emptyList(),
                key = YouTube.Item::key
            ) { song ->
                SmallSongItem(
                    song = song,
                    thumbnailSizePx = songThumbnailSizePx,
                    onClick = {
                        val mediaItem = song.asMediaItem
                        binder?.stopRadio()
                        binder?.player?.forcePlay(mediaItem)
                        binder?.setupRadio(
                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                        )
                    },
                )
            }
        }
    }
}
