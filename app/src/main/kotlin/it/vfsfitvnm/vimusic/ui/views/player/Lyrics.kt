package it.vfsfitvnm.vimusic.ui.views.player

import android.app.SearchManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.kugou.KuGou
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.PureBlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.DefaultDarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.onOverlayShimmer
import it.vfsfitvnm.vimusic.utils.SynchronizedLyrics
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.isShowingSynchronizedLyricsKey
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.verticalFadingEdge
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long,
    onLyricsUpdate: (Boolean, String, String) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        var isShowingSynchronizedLyrics by rememberPreference(isShowingSynchronizedLyricsKey, false)

        var isLoading by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        var isEditingLyrics by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        var lyrics by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf<String?>(".")
        }

        LaunchedEffect(mediaId, isShowingSynchronizedLyrics) {
            if (isShowingSynchronizedLyrics) {
                Database.synchronizedLyrics(mediaId)
            } else {
                Database.lyrics(mediaId)
            }.distinctUntilChanged().map flowMap@{ lyrics ->
                if (lyrics != null) return@flowMap lyrics

                isLoading = true

                if (isShowingSynchronizedLyrics) {
                    val mediaMetadata = mediaMetadataProvider()
                    var duration = withContext(Dispatchers.Main) {
                        durationProvider()
                    }

                    while (duration == C.TIME_UNSET) {
                        delay(100)
                        duration = withContext(Dispatchers.Main) {
                            durationProvider()
                        }
                    }

                    KuGou.lyrics(mediaMetadata.artist?.toString() ?: "", mediaMetadata.title?.toString() ?: "", duration / 1000)?.map {
                        it?.value
                    }
                } else {
                    YouTube.next(mediaId, null)?.map { nextResult -> nextResult.lyrics?.text()?.getOrNull() }
                }?.map { newLyrics ->
                    onLyricsUpdate(isShowingSynchronizedLyrics, mediaId, newLyrics ?: "")
                    isLoading = false
                    return@flowMap newLyrics ?: ""
                }

                isLoading = false
                null
            }.flowOn(Dispatchers.IO).collect { lyrics = it }
        }

        if (isEditingLyrics) {
            TextFieldDialog(
                hintText = stringResource(R.string.enter_lyrics),
                initialTextInput = lyrics ?: "",
                singleLine = false,
                maxLines = 10,
                isTextInputValid = { true },
                onDismiss = {
                    isEditingLyrics = false
                },
                onDone = {
                    query {
                        if (isShowingSynchronizedLyrics) {
                            Database.updateSynchronizedLyrics(mediaId, it)
                        } else {
                            Database.updateLyrics(mediaId, it)
                        }

                    }
                }
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onDismiss()
                        }
                    )
                }
                .fillMaxSize()
                .background(Color.Black.copy(0.8f))
        ) {
            AnimatedVisibility(
                visible = !isLoading && lyrics == null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "An error has occurred while fetching the ${if (isShowingSynchronizedLyrics) "synchronized " else ""}lyrics",
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = lyrics?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "${if (isShowingSynchronizedLyrics) "Synchronized l" else "L"}yrics are not available for this song",
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(4) { index ->
                        TextPlaceholder(
                            color = colorPalette.onOverlayShimmer,
                            modifier = Modifier
                                .alpha(1f - index * 0.05f)
                        )
                    }
                }
            } else {
                lyrics?.let { lyrics ->
                    if (lyrics.isNotEmpty() && lyrics != ".") {
                        if (isShowingSynchronizedLyrics) {
                            val density = LocalDensity.current
                            val player = LocalPlayerServiceBinder.current?.player ?: return@AnimatedVisibility

                            val synchronizedLyrics = remember(lyrics) {
                                SynchronizedLyrics(KuGou.Lyrics(lyrics).sentences) {
                                    player.currentPosition + 50
                                }
                            }

                            val lazyListState = rememberLazyListState(synchronizedLyrics.index, with (density) { size.roundToPx() } / 6)

                            LaunchedEffect(synchronizedLyrics) {
                                val center = with (density) { size.roundToPx() } / 6

                                while (isActive) {
                                    delay(50)
                                    if (synchronizedLyrics.update()) {
                                        lazyListState.animateScrollToItem(synchronizedLyrics.index, center)
                                    }
                                }
                            }

                            LazyColumn(
                                state = lazyListState,
                                userScrollEnabled = false,
                                contentPadding = PaddingValues(vertical = size / 2),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .verticalFadingEdge()
                            ) {
                                itemsIndexed(items = synchronizedLyrics.sentences) { index, sentence ->
                                    BasicText(
                                        text = sentence.second,
                                        style = typography.xs.center.medium.color(if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                    )
                                }
                            }
                        } else {
                            BasicText(
                                text = lyrics,
                                style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                                modifier = Modifier
                                    .nestedScroll(remember { nestedScrollConnectionProvider() })
                                    .verticalFadingEdge()
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = size / 4, horizontal = 32.dp)
                            )
                        }
                    }

                    val menuState = LocalMenuState.current

                    Image(
                        painter = painterResource(R.drawable.ellipsis_horizontal),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(DefaultDarkColorPalette.text),
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .clickable {
                                menuState.display {
                                    Menu {
                                        MenuEntry(
                                            icon = R.drawable.time,
                                            text = "Show ${if (isShowingSynchronizedLyrics) "un" else ""}synchronized lyrics",
                                            secondaryText = if (isShowingSynchronizedLyrics) null else "Provided by kugou.com",
                                            onClick = {
                                                menuState.hide()
                                                isShowingSynchronizedLyrics = !isShowingSynchronizedLyrics
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.pencil,
                                            text = stringResource(R.string.edit_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                isEditingLyrics = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.search,
                                            text = stringResource(R.string.search_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                val mediaMetadata = mediaMetadataProvider()

                                                val intent =
                                                    Intent(Intent.ACTION_WEB_SEARCH).apply {
                                                        putExtra(
                                                            SearchManager.QUERY,
                                                            "${mediaMetadata.title} ${mediaMetadata.artist} lyrics"
                                                        )
                                                    }

                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "No browser app found!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.download,
                                            text = stringResource(R.string.fetch_lyrics_again),
                                            onClick = {
                                                menuState.hide()
                                                query {
                                                    if (isShowingSynchronizedLyrics) {
                                                        Database.updateSynchronizedLyrics(mediaId, null)
                                                    } else {
                                                        Database.updateLyrics(mediaId, null)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            .padding(all = 8.dp)
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}
