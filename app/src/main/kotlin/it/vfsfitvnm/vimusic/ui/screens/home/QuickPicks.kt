package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.savers.DetailedSongSaver
import it.vfsfitvnm.vimusic.savers.YouTubeRelatedSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.savers.resultSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemShimmer
import it.vfsfitvnm.vimusic.ui.views.ArtistItem
import it.vfsfitvnm.vimusic.ui.views.ArtistItemShimmer
import it.vfsfitvnm.vimusic.ui.views.PlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistItemShimmer
import it.vfsfitvnm.vimusic.ui.views.SmallSongItem
import it.vfsfitvnm.vimusic.ui.views.SmallSongItemShimmer
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.produceSaveableOneShotState
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn

@ExperimentalAnimationApi
@Composable
fun QuickPicks(
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
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

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
    val artistThumbnailSizeDp = 64.dp
    val artistThumbnailSizePx = artistThumbnailSizeDp.px
    val playlistThumbnailSizeDp = 108.dp
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    BoxWithConstraints {
        val itemInHorizontalGridWidth = maxWidth * 0.9f

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LocalPlayerAwarePaddingValues.current)
        ) {
            Header(title = "Quick picks")

            relatedResult?.getOrNull()?.let { related ->
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                ) {
                    trending?.let { song ->
                        item {
                            SongItem(
                                thumbnailModel = song.thumbnailUrl?.thumbnail(songThumbnailSizePx),
                                title = song.title,
                                authors = song.artistsText,
                                durationText = null,
                                menuContent = { NonQueuedMediaItemMenu(mediaItem = song.asMediaItem) },
                                onClick = {
                                    val mediaItem = song.asMediaItem
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(mediaItem)
                                    binder?.setupRadio(
                                        NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                    )
                                },
                                modifier = Modifier
                                    .width(itemInHorizontalGridWidth)
                            )
                        }
                    }

                    items(
                        items = related.songs ?: emptyList(),
                        key = YouTube.Item.Song::key
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
                            modifier = Modifier
                                .width(itemInHorizontalGridWidth)
                        )
                    }
                }

                BasicText(
                    text = "Related albums",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((albumThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 2)
                ) {
                    items(
                        items = related.albums ?: emptyList(),
                        key = YouTube.Item.Album::key
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
                                .width(itemInHorizontalGridWidth)
                        )
                    }
                }

                BasicText(
                    text = "Similar artists",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((artistThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2))
                ) {
                    items(
                        items = related.artists ?: emptyList(),
                        key = YouTube.Item.Artist::key,
                    ) { artist ->
                        ArtistItem(
                            artist = artist,
                            thumbnailSizePx = artistThumbnailSizePx,
                            thumbnailSizeDp = artistThumbnailSizeDp,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onArtistClick(artist.key) }
                                )
                                .width(itemInHorizontalGridWidth)
                        )
                    }
                }

                BasicText(
                    text = "Playlists you might like",
                    style = typography.m.semiBold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp, bottom = 8.dp)
                )

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((playlistThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 2)
                ) {
                    items(
                        items = related.playlists ?: emptyList(),
                        key = YouTube.Item.Playlist::key,
                    ) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            thumbnailSizePx = playlistThumbnailSizePx,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onPlaylistClick(playlist.key) }
                                )
                                .width(itemInHorizontalGridWidth)
                        )
                    }
                }
            } ?: relatedResult?.exceptionOrNull()?.let {
                BasicText(
                    text = "An error has occurred",
                    style = typography.s.secondary.center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                )
            } ?: Column(
                modifier = Modifier
                    .shimmer()
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Black, Color.Transparent)
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                repeat(4) {
                    SmallSongItemShimmer(
                        thumbnailSizeDp = songThumbnailSizeDp,
                    )
                }

                TextPlaceholder(modifier = sectionTextModifier)

                repeat(2) {
                    AlbumItemShimmer(thumbnailSizeDp = albumThumbnailSizeDp)
                }

                TextPlaceholder(modifier = sectionTextModifier)

                ArtistItemShimmer(thumbnailSizeDp = artistThumbnailSizeDp)

                TextPlaceholder(modifier = sectionTextModifier)

                repeat(2) {
                    PlaylistItemShimmer(thumbnailSizeDp = playlistThumbnailSizeDp)
                }
            }
        }
    }
}
