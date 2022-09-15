package it.vfsfitvnm.vimusic.ui.views.player

import android.text.format.DateUtils
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.rememberRepeatMode
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Controls(
    mediaId: String,
    title: String?,
    artist: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val repeatMode by rememberRepeatMode(binder.player)

    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    val likedAt by remember(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged()
    }.collectAsState(initial = null, context = Dispatchers.IO)

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        BasicText(
            text = title ?: "",
            style = typography.l.bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        BasicText(
            text = artist ?: "",
            style = typography.s.semiBold.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier
                .weight(0.5f)
        )

        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = colorPalette.text,
            backgroundColor = colorPalette.background2,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicText(
                text = DateUtils.formatElapsedTime((scrubbingPosition ?: position) / 1000),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                BasicText(
                    text = DateUtils.formatElapsedTime(duration / 1000),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(if (likedAt == null) R.drawable.heart_outline else R.drawable.heart),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.favoritesIcon),
                modifier = Modifier
                    .clickable {
                        val currentMediaItem = binder.player.currentMediaItem
                        query {
                            if (Database.like(mediaId, if (likedAt == null) System.currentTimeMillis() else null) == 0) {
                                currentMediaItem?.takeIf { it.mediaId == mediaId }?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                            }
                        }
                    }
                    .weight(1f)
                    .size(24.dp)
            )

            Image(
                painter = painterResource(R.drawable.play_skip_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(onClick = binder.player::forceSeekToPrevious)
                    .weight(1f)
                    .size(24.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(playPauseRoundness))
                    .clickable {
                        if (shouldBePlaying) {
                            binder.player.pause()
                        } else {
                            if (binder.player.playbackState == Player.STATE_IDLE) {
                                binder.player.prepare()
                            }
                            binder.player.play()
                        }
                    }
                    .background(colorPalette.background2)
                    .size(64.dp)
            ) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Image(
                painter = painterResource(R.drawable.play_skip_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(onClick = binder.player::forceSeekToNext)
                    .weight(1f)
                    .size(24.dp)
            )

            Image(
                painter = painterResource(R.drawable.infinite),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    if (repeatMode == Player.REPEAT_MODE_ONE) {
                        colorPalette.text
                    } else {
                        colorPalette.textDisabled
                    }
                ),
                modifier = Modifier
                    .clickable {
                        binder.player.repeatMode = when (binder.player.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                            else -> Player.REPEAT_MODE_ONE
                        }
                    }
                    .weight(1f)
                    .size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )
    }
}
