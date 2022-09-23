package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.LoadingOrError
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.align
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import kotlinx.coroutines.delay

@Composable
fun OnlineSearchTab(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    isOpenableUrl: Boolean,
    onSearch: (String) -> Unit,
    onUri: () -> Unit,
    viewModel: OnlineSearchTabViewModel = viewModel(
        key = textFieldValue.text,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OnlineSearchTabViewModel(textFieldValue.text) as T
            }
        }
    )
) {
    val (colorPalette, typography) = LocalAppearance.current

    val timeIconPainter = painterResource(R.drawable.time)
    val closeIconPainter = painterResource(R.drawable.close)
    val arrowForwardIconPainter = painterResource(R.drawable.arrow_forward)
    val rippleIndication = rememberRipple(bounded = true)

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

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
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                    )
                },
                actionsContent = {
                    BasicText(
                        text = if (isOpenableUrl) "Open url" else "Search",
                        style = typography.xxs.medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = textFieldValue.text.isNotEmpty()) {
                                if (isOpenableUrl) onUri() else onSearch(textFieldValue.text)
                            }
                            .background(colorPalette.background2)
                            .padding(all = 8.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    BasicText(
                        text = "Clear",
                        style = typography.xxs.medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = textFieldValue.text.isNotEmpty()) {
                                onTextFieldValueChanged(TextFieldValue())
                            }
                            .background(colorPalette.background2)
                            .padding(all = 8.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
            )
        }

        items(
            items = viewModel.history,
            key = SearchQuery::id
        ) { searchQuery ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        indication = rippleIndication,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onSearch(searchQuery.query) }
                    )
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                        .paint(
                            painter = timeIconPainter,
                            colorFilter = ColorFilter.tint(colorPalette.textDisabled)
                        )
                )

                BasicText(
                    text = searchQuery.query,
                    style = typography.s.secondary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                )

                Spacer(
                    modifier = Modifier
                        .clickable {
                            query {
                                Database.delete(searchQuery)
                            }
                        }
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                        .paint(
                            painter = closeIconPainter,
                            colorFilter = ColorFilter.tint(colorPalette.textDisabled)
                        )
                )

                Spacer(
                    modifier = Modifier
                        .clickable {
                            onTextFieldValueChanged(
                                TextFieldValue(
                                    text = searchQuery.query,
                                    selection = TextRange(searchQuery.query.length)
                                )
                            )
                        }
                        .rotate(225f)
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                        .paint(
                            painter = arrowForwardIconPainter,
                            colorFilter = ColorFilter.tint(colorPalette.textDisabled)
                        )
                )
            }
        }

        viewModel.suggestionsResult?.getOrNull()?.let { suggestions ->
            items(items = suggestions) { suggestion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            indication = rippleIndication,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onSearch(suggestion) }
                        )
                        .fillMaxWidth()
                        .padding(all = 16.dp)
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

                    Spacer(
                        modifier = Modifier
                            .clickable {
                                onTextFieldValueChanged(
                                    TextFieldValue(
                                        text = suggestion,
                                        selection = TextRange(suggestion.length)
                                    )
                                )
                            }
                            .rotate(225f)
                            .padding(horizontal = 8.dp)
                            .size(22.dp)
                            .paint(
                                painter = arrowForwardIconPainter,
                                colorFilter = ColorFilter.tint(colorPalette.textDisabled)
                            )
                    )
                }
            }
        } ?: viewModel.suggestionsResult?.exceptionOrNull()?.let { throwable ->
            item {
                LoadingOrError(errorMessage = throwable.javaClass.canonicalName) {}
            }
        }
    }
}
