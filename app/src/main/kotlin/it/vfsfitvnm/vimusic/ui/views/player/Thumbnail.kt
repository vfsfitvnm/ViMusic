package it.vfsfitvnm.vimusic.ui.views.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.rememberError
import it.vfsfitvnm.vimusic.utils.rememberMediaItemIndex
import it.vfsfitvnm.vimusic.utils.thumbnail

@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val mediaItemIndex by rememberMediaItemIndex(player)

    val error by rememberError(player)

    if (error == null) {
        AnimatedContent(
            targetState = mediaItemIndex,
            transitionSpec = {
                val slideDirection =
                    if (targetState > initialState) AnimatedContentScope.SlideDirection.Left else AnimatedContentScope.SlideDirection.Right

                (slideIntoContainer(slideDirection) + fadeIn() with
                        slideOutOfContainer(slideDirection) + fadeOut()).using(
                    SizeTransform(clip = false)
                )
            },
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1f)
        ) { currentMediaItemIndex ->
            val mediaItem = remember(currentMediaItemIndex) {
                player.getMediaItemAt(currentMediaItemIndex)
            }

            Box(
                modifier = Modifier
                    .clip(ThumbnailRoundness.shape)
                    .size(thumbnailSizeDp)
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onShowLyrics(true)
                                },
                                onLongPress = {
                                    onShowStatsForNerds(true)
                                }
                            )
                        }
                        .fillMaxSize()
                )

                Lyrics(
                    mediaId = mediaItem.mediaId,
                    isDisplayed = isShowingLyrics,
                    onDismiss = {
                        onShowLyrics(false)
                    },
                    onLyricsUpdate = { mediaId, lyrics ->
                        if (Database.updateLyrics(mediaId, lyrics) == 0) {
                            if (mediaId == mediaItem.mediaId) {
                                Database.insert(mediaItem) { song ->
                                    song.copy(lyrics = lyrics)
                                }
                            }
                        }
                    },
                    size = thumbnailSizeDp,
                    mediaMetadataProvider = mediaItem::mediaMetadata,
                    nestedScrollConnectionProvider = nestedScrollConnectionProvider,
                )

                StatsForNerds(
                    mediaId = mediaItem.mediaId,
                    isDisplayed = isShowingStatsForNerds,
                    onDismiss = {
                        onShowStatsForNerds(false)
                    },
                    modifier = Modifier
                )
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .padding(bottom = 32.dp)
                .padding(horizontal = 32.dp)
                .size(thumbnailSizeDp)
        ) {
            LoadingOrError(
                errorMessage = error?.javaClass?.canonicalName,
                onRetry = {
                    player.playWhenReady = true
                    player.prepare()
                }
            ) {}
        }
    }
}
