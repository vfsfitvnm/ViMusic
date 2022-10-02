package it.vfsfitvnm.vimusic.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.PrimaryButton
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.ui.views.SongItemPlaceholder
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint

@ExperimentalAnimationApi
@Composable
fun ArtistOverview(
    artist: Artist?,
    youtubeArtistPage: Innertube.ArtistPage?,
    isLoading: Boolean,
    isError: Boolean,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    bookmarkIconContent: @Composable () -> Unit,
    shareIconContent: @Composable () -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    BoxWithConstraints {
        val thumbnailSizeDp = maxWidth - Dimensions.navigationRailWidth
        val thumbnailSizePx = (thumbnailSizeDp - 32.dp).px

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LocalPlayerAwarePaddingValues.current)
        ) {
            when {
                artist != null -> {
                    Header(title = artist.name ?: "Unknown") {
                        youtubeArtistPage?.radioEndpoint?.let { radioEndpoint ->
                            SecondaryTextButton(
                                text = "Start radio",
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.playRadio(radioEndpoint)
                                }
                            )
                        }

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

                    when {
                        youtubeArtistPage != null -> {
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
                                                .clickable(
                                                    indication = rememberRipple(bounded = true),
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = onViewAllSongsClick
                                                ),
                                        )
                                    }
                                }

                                songs.forEach { song ->
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
                                        }
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
                                                .clickable(
                                                    indication = rememberRipple(bounded = true),
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = onViewAllAlbumsClick
                                                ),
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
                                                .clickable(
                                                    indication = rememberRipple(bounded = true),
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = { onAlbumClick(album.key) }
                                                )
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
                                                .clickable(
                                                    indication = rememberRipple(bounded = true),
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = onViewAllSinglesClick
                                                ),
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
                                                .clickable(
                                                    indication = rememberRipple(bounded = true),
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = { onAlbumClick(album.key) }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        isError -> ErrorText()
                        isLoading -> ShimmerHost {
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
                isError -> ErrorText()
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

@Composable
fun ColumnScope.ErrorText() {
    BasicText(
        text = "An error has occurred",
        style = LocalAppearance.current.typography.s.secondary.center,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(all = 16.dp)
    )
}

@Composable
fun ShimmerHost(content: @Composable ColumnScope.() -> Unit) {
    Column(
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
            },
        content = content
    )
}
