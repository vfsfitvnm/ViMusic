package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.ui.components.OutcomeItem
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.LocalYoutubePlayer
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.toNullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun IntentVideoScreen(videoId: String) {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val colorPalette = LocalColorPalette.current
            val density = LocalDensity.current
            val player = LocalYoutubePlayer.current

            val mediaItem by produceState<Outcome<MediaItem>>(initialValue = Outcome.Loading) {
                value = withContext(Dispatchers.IO) {
                    Database.songWithInfo(videoId)?.let { songWithInfo ->
                        Outcome.Success(songWithInfo.asMediaItem)
                    } ?: YouTube.getQueue(videoId).toNullable()
                        ?.map(YouTube.Item.Song::asMediaItem)
                    ?: Outcome.Error.Network
                }
            }

            Column(
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
            ) {
                OutcomeItem(
                    outcome = mediaItem,
                    onLoading = {
                        SmallSongItemShimmer(
                            shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View),
                            thumbnailSizeDp = 54.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                ) { mediaItem ->
                    SongItem(
                        mediaItem = mediaItem,
                        thumbnailSize = remember {
                            density.run {
                                54.dp.roundToPx()
                            }
                        },
                        onClick = {
                            player?.mediaController?.forcePlay(mediaItem)
                            pop()
                        },
                        menuContent = {
                            NonQueuedMediaItemMenu(mediaItem = mediaItem)
                        }
                    )
                }
            }
        }
    }
}
