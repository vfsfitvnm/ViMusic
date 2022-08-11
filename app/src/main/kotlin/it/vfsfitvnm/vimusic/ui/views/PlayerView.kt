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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.themed.BaseMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.collapsedPlayerProgressBar
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

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current

    binder?.player ?: return

    val nullableMediaItem by rememberMediaItem(binder.player)

    val mediaItem = nullableMediaItem ?: return

    val shouldBePlaying by rememberShouldBePlaying(binder.player)
    val positionAndDuration by rememberPositionAndDuration(binder.player)

    var xOffset = 0f

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
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                xOffset = 0f
                            },
                            onDragEnd = {
                                if (xOffset >= 30) {
                                    binder.player.seekToPreviousMediaItem()
                                } else if (xOffset <= -30) {
                                    binder.player.seekToNextMediaItem()
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()

                            xOffset += dragAmount
                        }
                    }
                    .background(colorPalette.background1)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .drawBehind {
                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
                ) {
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.player.songPreview.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .size(48.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
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
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer)
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
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable(onClick = binder.player::seekToNext)
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.play_skip_forward),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )
            }
        }
    ) {
        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        val paddingValues = WindowInsets.navigationBars.asPaddingValues()
        val playerBottomSheetState = rememberBottomSheetState(64.dp + paddingValues.calculateBottomPadding(), layoutState.expandedBound)

        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(colorPalette.background1)
                        .padding(
                            top = 32.dp + paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            end = paddingValues.calculateEndPadding(layoutDirection),
                            bottom = playerBottomSheetState.collapsedBound
                        )
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
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f),
                        onGlobalRouteEmitted = layoutState::collapseSoft
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(colorPalette.background1)
                        .padding(
                            top = 54.dp + paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            end = paddingValues.calculateEndPadding(layoutDirection),
                            bottom = playerBottomSheetState.collapsedBound
                        )
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
                        position = positionAndDuration.first,
                        duration = positionAndDuration.second,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        onGlobalRouteEmitted = layoutState::collapseSoft
                    )
                }
            }
        }

        PlayerBottomSheet(
            layoutState = playerBottomSheetState,
            onGlobalRouteEmitted = layoutState::collapseSoft,
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
                    Spacer(
                        modifier = Modifier
                            .width(4.dp)
                    )
                }
            },
            backgroundColorProvider = { colorPalette.background2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}
