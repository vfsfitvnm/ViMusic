package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.savers.DetailedSongListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.align
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

//context(ProduceStateScope<T>)
//fun <T> Flow<T>.distinctUntilChangedWithProducedState() =
//    distinctUntilChanged { old, new -> new != old && new != value }

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LocalSongSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val items by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = DetailedSongListSaver,
        key1 = textFieldValue.text
    ) {
        Database
            .search("%${textFieldValue.text}%")
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val thumbnailSize = Dimensions.thumbnails.song.px

    LazyColumn(
        contentPadding = LocalPlayerAwarePaddingValues.current,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0
        ) {
            Header(
                titleContent = {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = onTextFieldValueChanged,
                        textStyle = typography.xxl.medium.align(TextAlign.End),
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        cursorBrush = SolidColor(colorPalette.text),
                        decorationBox = { innerTextField ->
                            Box {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = textFieldValue.text.isEmpty(),
                                    enter = fadeIn(tween(200)),
                                    exit = fadeOut(tween(200)),
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                ) {
                                    BasicText(
                                        text = "Enter a name",
                                        maxLines = 1,
                                        style = typography.xxl.secondary
                                    )
                                }

                                innerTextField()
                            }
                        }
                    )
                },
                actionsContent = {
                    if (textFieldValue.text.isNotEmpty()) {
                        BasicText(
                            text = "Clear",
                            style = typography.xxs.medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onTextFieldValueChanged(TextFieldValue()) }
                                .background(colorPalette.background2)
                                .padding(all = 8.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            )
        }

        items(
            items = items,
            key = DetailedSong::id,
        ) { song ->
            SongItem(
                song = song,
                thumbnailSize = thumbnailSize,
                onClick = {
                    val mediaItem = song.asMediaItem
                    binder?.stopRadio()
                    binder?.player?.forcePlay(mediaItem)
                    binder?.setupRadio(
                        NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                    )
                },
                menuContent = { InHistoryMediaItemMenu(song = song) },
                modifier = Modifier
                    .animateItemPlacement()
            )
        }
    }
}