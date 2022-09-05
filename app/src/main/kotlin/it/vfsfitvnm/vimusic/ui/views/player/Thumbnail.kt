package it.vfsfitvnm.vimusic.ui.views.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import it.vfsfitvnm.vimusic.service.LoginRequiredException
import it.vfsfitvnm.vimusic.service.PlayableFormatNotFoundException
import it.vfsfitvnm.vimusic.service.UnplayableException
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.rememberError
import it.vfsfitvnm.vimusic.utils.rememberMediaItemIndex
import it.vfsfitvnm.vimusic.utils.thumbnail
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

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

    AnimatedContent(
        targetState = mediaItemIndex,
        transitionSpec = {
            val duration = 500
            val slideDirection =
                if (targetState > initialState) AnimatedContentScope.SlideDirection.Left else AnimatedContentScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        contentAlignment = Alignment.Center
    ) { currentMediaItemIndex ->
        val mediaItem = remember(currentMediaItemIndex) {
            player.getMediaItemAt(currentMediaItemIndex)
        }

        Box(
            modifier = modifier
                .aspectRatio(1f)
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
                            onTap = { onShowLyrics(true) },
                            onLongPress = { onShowStatsForNerds(true) }
                        )
                    }
                    .fillMaxSize()
            )

            Lyrics(
                mediaId = mediaItem.mediaId,
                isDisplayed = isShowingLyrics && error == null,
                onDismiss = { onShowLyrics(false) },
                onLyricsUpdate = { areSynchronized, mediaId, lyrics ->
                    if (areSynchronized) {
                        if (Database.updateSynchronizedLyrics(mediaId, lyrics) == 0) {
                            if (mediaId == mediaItem.mediaId) {
                                Database.insert(mediaItem) { song ->
                                    song.copy(synchronizedLyrics = lyrics)
                                }
                            }
                        }
                    } else {
                        if (Database.updateLyrics(mediaId, lyrics) == 0) {
                            if (mediaId == mediaItem.mediaId) {
                                Database.insert(mediaItem) { song ->
                                    song.copy(lyrics = lyrics)
                                }
                            }
                        }
                    }
                },
                size = thumbnailSizeDp,
                mediaMetadataProvider = mediaItem::mediaMetadata,
                durationProvider = player::getDuration,
                nestedScrollConnectionProvider = nestedScrollConnectionProvider,
            )

            StatsForNerds(
                mediaId = mediaItem.mediaId,
                isDisplayed = isShowingStatsForNerds && error == null,
                onDismiss = { onShowStatsForNerds(false) }
            )

            PlaybackError(
                isDisplayed = error != null,
                messageProvider = {
                    when (error?.cause?.cause) {
                        is UnresolvedAddressException, is UnknownHostException -> "A network error has occurred"
                        is PlayableFormatNotFoundException -> "Couldn't find a playable audio format"
                        is UnplayableException -> "The original video source of this song has been deleted"
                        is LoginRequiredException -> "This song cannot be played due to server restrictions"
                        else -> "An unknown playback error has occurred"
                    }
                },
                onDismiss = player::prepare
            )
        }
    }
}
