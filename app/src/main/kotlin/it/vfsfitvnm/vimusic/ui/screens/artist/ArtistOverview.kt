package it.vfsfitvnm.vimusic.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.ShimmerHost
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistOverview(
    youtubeArtistPage: Innertube.ArtistPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    thumbnailContent: @Composable ColumnScope.() -> Unit,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    Box {
        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LocalPlayerAwarePaddingValues.current)
        ) {
            headerContent {
                youtubeArtistPage?.radioEndpoint?.let { radioEndpoint ->
                    SecondaryTextButton(
                        text = "Start radio",
                        onClick = {
                            binder?.stopRadio()
                            binder?.playRadio(radioEndpoint)
                        }
                    )
                }
            }

            thumbnailContent()

            if (youtubeArtistPage != null) {
                youtubeArtistPage.songs?.let { songs ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        BasicText(
                            text = "Songs",
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        youtubeArtistPage.songsEndpoint?.let {
                            BasicText(
                                text = "View all",
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier
                                    .clickable(onClick = onViewAllSongsClick),
                            )
                        }
                    }

                    songs.forEach { song ->
                        SongItem(
                            song = song,
                            thumbnailSizeDp = songThumbnailSizeDp,
                            thumbnailSizePx = songThumbnailSizePx,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(mediaItem = song.asMediaItem)
                                        }
                                    },
                                    onClick = {
                                        val mediaItem = song.asMediaItem
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(mediaItem)
                                        binder?.setupRadio(
                                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                        )
                                    }
                                )
                        )
                    }
                }

                youtubeArtistPage.albums?.let { albums ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        BasicText(
                            text = "Albums",
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        youtubeArtistPage.albumsEndpoint?.let {
                            BasicText(
                                text = "View all",
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier
                                    .clickable(onClick = onViewAllAlbumsClick),
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(
                            items = albums,
                            key = Innertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSizePx = albumThumbnailSizePx,
                                thumbnailSizeDp = albumThumbnailSizeDp,
                                alternative = true,
                                modifier = Modifier
                                    .clickable(onClick = { onAlbumClick(album.key) })
                            )
                        }
                    }
                }

                youtubeArtistPage.singles?.let { singles ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        BasicText(
                            text = "Singles",
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        youtubeArtistPage.singlesEndpoint?.let {
                            BasicText(
                                text = "View all",
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier
                                    .clickable(onClick = onViewAllSinglesClick),
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(
                            items = singles,
                            key = Innertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSizePx = albumThumbnailSizePx,
                                thumbnailSizeDp = albumThumbnailSizeDp,
                                alternative = true,
                                modifier = Modifier
                                    .clickable(onClick = { onAlbumClick(album.key) })
                            )
                        }
                    }
                }
            } else {
                ShimmerHost {
                    TextPlaceholder(modifier = sectionTextModifier)

                    repeat(5) {
                        SongItemPlaceholder(
                            thumbnailSizeDp = songThumbnailSizeDp,
                        )
                    }

                    repeat(2) {
                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                AlbumItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }
                    }
                }
            }
        }

        youtubeArtistPage?.shuffleEndpoint?.let { shuffleEndpoint ->
            PrimaryButton(
                iconId = R.drawable.shuffle,
                onClick = {
                    binder?.stopRadio()
                    binder?.playRadio(shuffleEndpoint)
                }
            )
        }
    }
}
