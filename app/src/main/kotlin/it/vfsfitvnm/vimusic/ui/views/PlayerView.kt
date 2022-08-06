package it.vfsfitvnm.vimusic.ui.views

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.audiofx.AudioEffect
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.player.Controls
import it.vfsfitvnm.vimusic.ui.views.player.Thumbnail
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColor


@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalAnimationApi
@Composable
fun PlayerView(
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val menuState = LocalMenuState.current

    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    binder?.player ?: return

    val nullableMediaItem by rememberMediaItem(binder.player)

    val mediaItem = nullableMediaItem ?: return

    val shouldBePlaying by rememberShouldBePlaying(binder.player)
    val positionAndDuration by rememberPositionAndDuration(binder.player)

    var xOffset = 0f


    var backgroundColor : Color = colorPalette.elevatedBackground
    var elevatedBackgroundColor : Color = colorPalette.background

    val imageRequest = ImageRequest.Builder(context)
        .data(mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.player.songPreview.px))
        .allowHardware(false)
        .target() { drawable ->
            var img = drawable.toBitmap()
            var bitmap = Bitmap.createBitmap(img)
            Palette.from(bitmap).generate { palette ->
                if (palette?.getDarkMutedColor(0x000000) == 0x000000) {
                    backgroundColor = Color(palette.getDominantColor(0x000000)!!.toColor().toArgb()+(0xff16171d).toInt())
                    elevatedBackgroundColor = Color(palette!!.getDominantColor(0x000000)!!.toColor().toArgb())
                }else {
                    backgroundColor = Color(palette!!.getDarkMutedColor(0x000000)!!.toColor().toArgb()+(0xff16171d).toInt())
                    elevatedBackgroundColor = Color(palette!!.getDarkMutedColor(0x000000)!!.toColor().toArgb())
                }

            }
        }
        .build()
    ImageLoader(context).enqueue(imageRequest)

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        onDismiss = {
            binder.stopRadio()
            binder.player.clearMediaItems()
        },
        collapsedContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .pointerInput(Unit) {

                        detectHorizontalDragGestures(
                            onDragStart = {
                                xOffset = 0f
                            },
                            onDragEnd = {
                                if (xOffset >= 30) {
                                    binder.player.seekToPrevious()
                                } else if (xOffset <= -30) {
                                    binder.player.seekToNext()
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()

                            xOffset += dragAmount
                        }
                    }
                    .background(backgroundColor)
                    .fillMaxSize()
                    .drawBehind {
                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue
                        val offset = Dimensions.thumbnails.player.songPreview.toPx()

                        drawLine(
                            color = colorPalette.text,
                            start = Offset(x = offset, y = 1.dp.toPx()),
                            end = Offset(
                                x = ((size.width - offset) * progress) + offset,
                                y = 1.dp.toPx()
                            ),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.player.songPreview.px),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Dimensions.thumbnails.player.songPreview)
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    BasicText(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        style = typography.xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
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
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable(onClick = binder.player::seekToNext)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.play_skip_forward),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )
                }

            }
        }
    ) {
        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .background(backgroundColor)
                        .padding(top = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.66f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Thumbnail(
                            isShowingLyrics = isShowingLyrics,
                            onShowLyrics = { isShowingLyrics = it },
                            isShowingStatsForNerds = isShowingStatsForNerds,
                            onShowStatsForNerds = { isShowingStatsForNerds = it },
                            nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                        )
                    }

                    Controls(
                        mediaItem = mediaItem,
                        shouldBePlaying = shouldBePlaying,
                        backgroundColor = elevatedBackgroundColor,
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .background(backgroundColor)
                        .padding(top = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1.25f)
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                    ) {
                        Thumbnail(
                            isShowingLyrics = isShowingLyrics,
                            onShowLyrics = { isShowingLyrics = it },
                            isShowingStatsForNerds = isShowingStatsForNerds,
                            onShowStatsForNerds = { isShowingStatsForNerds = it },
                            nestedScrollConnectionProvider = layoutState::nestedScrollConnection,
                        )
                    }

                    Controls(
                        mediaItem = mediaItem,
                        shouldBePlaying = shouldBePlaying,
                        backgroundColor = elevatedBackgroundColor,
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        PlayerBottomSheet(
            layoutState = rememberBottomSheetState(64.dp, layoutState.expandedBound),
            onGlobalRouteEmitted = layoutState::collapseSoft,
            backgroundColor = elevatedBackgroundColor,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight()
                ) {
                    Image(
                        painter = painterResource(R.drawable.text),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (isShowingLyrics) colorPalette.text else colorPalette.textDisabled),
                        modifier = Modifier
                            .clickable {
                                isShowingStatsForNerds = false
                                isShowingLyrics = !isShowingLyrics
                            }
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.information),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (isShowingStatsForNerds) colorPalette.text else colorPalette.textDisabled),
                        modifier = Modifier
                            .clickable {
                                isShowingLyrics = false
                                isShowingStatsForNerds = !isShowingStatsForNerds
                            }
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.ellipsis_horizontal),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable {
                                menuState.display {
                                    val resultRegistryOwner =
                                        LocalActivityResultRegistryOwner.current

                                    BaseMediaItemMenu(
                                        mediaItem = mediaItem,
                                        onStartRadio = {
                                            binder.stopRadio()
                                            binder.player.seamlessPlay(mediaItem)
                                            binder.setupRadio(
                                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                            )
                                        },
                                        onGoToEqualizer = {
                                            val intent =
                                                Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                                    putExtra(
                                                        AudioEffect.EXTRA_AUDIO_SESSION,
                                                        binder.player.audioSessionId
                                                    )
                                                    putExtra(
                                                        AudioEffect.EXTRA_PACKAGE_NAME,
                                                        context.packageName
                                                    )
                                                    putExtra(
                                                        AudioEffect.EXTRA_CONTENT_TYPE,
                                                        AudioEffect.CONTENT_TYPE_MUSIC
                                                    )
                                                }

                                            if (intent.resolveActivity(context.packageManager) != null) {
                                                val contract =
                                                    ActivityResultContracts.StartActivityForResult()

                                                resultRegistryOwner?.activityResultRegistry
                                                    ?.register("", contract) {}
                                                    ?.launch(intent)
                                            } else {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "No equalizer app found!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        },
                                        onSetSleepTimer = {},
                                        onDismiss = menuState::hide,
                                        onGlobalRouteEmitted = layoutState::collapseSoft,
                                    )
                                }
                            }
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
   }
}
