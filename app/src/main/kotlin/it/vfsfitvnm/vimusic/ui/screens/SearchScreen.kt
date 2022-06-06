package it.vfsfitvnm.vimusic.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.OutcomeItem
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun SearchScreen(
    initialTextInput: String,
    onSearch: (String) -> Unit
) {
    var textFieldValue by rememberSaveable(initialTextInput, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val searchSuggestions by produceState<Outcome<List<String>?>>(
        initialValue = Outcome.Initial,
        key1 = textFieldValue
    ) {
        value = if (textFieldValue.text.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                YouTube.getSearchSuggestions(textFieldValue.text)
            }
        } else {
            Outcome.Initial
        }
    }

    val history by remember(textFieldValue.text) {
        Database.getRecentQueries("%${textFieldValue.text}%").distinctUntilChanged { old, new ->
            old.size == new.size
        }
    }.collectAsState(initial = null, context = Dispatchers.IO)

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
            val typography = LocalTypography.current

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                delay(300)
                focusRequester.requestFocus()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                TopAppBar(
                    modifier = Modifier
                        .height(52.dp)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                        },
                        textStyle = typography.m.medium,
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (textFieldValue.text.isNotEmpty()) {
                                    onSearch(textFieldValue.text)
                                }
                            }
                        ),
                        cursorBrush = SolidColor(colorPalette.text),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.chevron_back),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.text),
                                    modifier = Modifier
                                        .clickable {
                                            pop()
                                            focusRequester.freeFocus()
                                        }
                                        .padding(vertical = 8.dp)
                                        .padding(horizontal = 16.dp)
                                        .size(24.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = textFieldValue.text.isEmpty(),
                                        enter = fadeIn(tween(100)),
                                        exit = fadeOut(tween(100)),
                                    ) {
                                        BasicText(
                                            text = "Enter a song, an album, an artist name...",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = typography.m.secondary,
                                        )
                                    }

                                    innerTextField()
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 64.dp)
                ) {
                    history?.forEach { searchQuery ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onSearch(searchQuery.query)
                                }
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.time),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.darkGray),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(20.dp)
                            )

                            BasicText(
                                text = searchQuery.query,
                                style = typography.s.secondary,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                            )

                            Image(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.darkGray),
                                modifier = Modifier
                                    .clickable {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            Database.delete(searchQuery)
                                        }
                                    }
                                    .padding(horizontal = 8.dp)
                                    .size(20.dp)
                            )

                            Image(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.darkGray),
                                modifier = Modifier
                                    .clickable {
                                        textFieldValue = TextFieldValue(
                                            text = searchQuery.query,
                                            selection = TextRange(searchQuery.query.length)
                                        )
                                    }
                                    .rotate(225f)
                                    .padding(horizontal = 8.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                    OutcomeItem(
                        outcome = searchSuggestions
                    ) { suggestions ->
                        suggestions?.forEach { suggestion ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        onSearch(suggestion)
                                    }
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(20.dp)
                                )

                                BasicText(
                                    text = suggestion,
                                    style = typography.s.secondary,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .weight(1f)
                                )


                                Image(
                                    painter = painterResource(R.drawable.arrow_forward),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.darkGray),
                                    modifier = Modifier
                                        .clickable {
                                            textFieldValue = TextFieldValue(
                                                text = suggestion,
                                                selection = TextRange(suggestion.length)
                                            )
                                        }
                                        .rotate(225f)
                                        .padding(horizontal = 8.dp)
                                        .size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
