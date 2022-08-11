package it.vfsfitvnm.vimusic.ui.screens

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun SearchScreen(initialTextInput: String, onSearch: (String) -> Unit, onUri: (Uri) -> Unit) {
    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette, typography) = LocalAppearance.current
            val layoutDirection = LocalLayoutDirection.current
            val paddingValues = WindowInsets.systemBars.asPaddingValues()

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

            val searchSuggestionsResult by produceState<Result<List<String>?>?>(
                initialValue = null,
                key1 = textFieldValue
            ) {
                value = if (textFieldValue.text.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        YouTube.getSearchSuggestions(textFieldValue.text)
                    }
                } else {
                    null
                }
            }

            val history by remember(textFieldValue.text) {
                Database.queries("%${textFieldValue.text}%").distinctUntilChanged { old, new ->
                    old.size == new.size
                }
            }.collectAsState(initial = null, context = Dispatchers.IO)

            val isOpenableUrl = remember(textFieldValue.text) {
                listOf(
                    "https://www.youtube.com/watch?",
                    "https://music.youtube.com/watch?",
                    "https://m.youtube.com/watch?",
                    "https://www.youtube.com/playlist?",
                    "https://music.youtube.com/playlist?",
                    "https://m.youtube.com/playlist?",
                    "https://youtu.be/",
                ).any(textFieldValue.text::startsWith)
            }

            LaunchedEffect(Unit) {
                delay(300)
                focusRequester.requestFocus()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        top = paddingValues.calculateTopPadding(),
                    )
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            text = stringResource(R.string.search_hint),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = typography.m.secondary,
                                        )
                                    }

                                    innerTextField()
                                }

                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            textFieldValue = TextFieldValue()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .background(
                                            color = colorPalette.background1,
                                            shape = CircleShape
                                        )
                                        .size(28.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(14.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }

                if (isOpenableUrl) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(bounded = true),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onUri(textFieldValue.text.toUri())
                                }
                            )
                            .fillMaxWidth()
                            .background(colorPalette.background1)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.link),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint( Color.Black),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp)
                        )

                        BasicText(
                            text = stringResource(R.string.open_url),
                            style = typography.s.secondary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = Dimensions.collapsedPlayer + paddingValues.calculateBottomPadding()
                    )
                ) {
                    items(
                        items = history ?: emptyList(),
                        key = SearchQuery::id
                    ) { searchQuery ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(
                                    indication = rememberRipple(bounded = true),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onSearch(searchQuery.query) }
                                )
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.time),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.textDisabled),
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
                                colorFilter = ColorFilter.tint(colorPalette.textDisabled),
                                modifier = Modifier
                                    .clickable {
                                        query {
                                            Database.delete(searchQuery)
                                        }
                                    }
                                    .padding(horizontal = 8.dp)
                                    .size(20.dp)
                            )

                            Image(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.textDisabled),
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

                    searchSuggestionsResult?.getOrNull()?.let { suggestions ->
                        items(items = suggestions) { suggestion ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(
                                        indication = rememberRipple(bounded = true),
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { onSearch(suggestion) }
                                    )
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
                                    colorFilter = ColorFilter.tint(Color.Black),
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
                    } ?: searchSuggestionsResult?.exceptionOrNull()?.let { throwable ->
                        item {
                            LoadingOrError(errorMessage = throwable.javaClass.canonicalName) {}
                        }
                    }
                }
            }
        }
    }
}
