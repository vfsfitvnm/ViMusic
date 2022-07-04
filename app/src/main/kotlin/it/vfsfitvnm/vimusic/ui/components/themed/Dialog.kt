package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.vfsfitvnm.vimusic.ui.components.ChunkyButton
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.delay

@Composable
fun TextFieldDialog(
    hintText: String,
    onDismiss: () -> Unit,
    onDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    doneText: String = "Done",
    initialTextInput: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    onCancel: () -> Unit = onDismiss,
    isTextInputValid: (String) -> Boolean = { it.isNotEmpty() }
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    var textFieldValue by rememberSaveable(initialTextInput, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
            },
            textStyle = typography.xs.semiBold.center,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isTextInputValid(textFieldValue.text)) {
                        onDismiss()
                        onDone(textFieldValue.text)
                    }
                }
            ),
            cursorBrush = SolidColor(colorPalette.text),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = textFieldValue.text.isEmpty(),
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(100)),
                    ) {
                        BasicText(
                            text = hintText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typography.xs.semiBold.secondary,
                        )
                    }

                    innerTextField()
                }
            },
            modifier = Modifier
                .padding(all = 16.dp)
                .weight(weight = 1f, fill = false)
                .focusRequester(focusRequester)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ChunkyButton(
                backgroundColor = Color.Transparent,
                text = cancelText,
                textStyle = typography.xs.semiBold,
                shape = RoundedCornerShape(36.dp),
                onClick = onCancel
            )

            ChunkyButton(
                backgroundColor = colorPalette.primaryContainer,
                text = doneText,
                textStyle = typography.xs.semiBold.color(colorPalette.onPrimaryContainer),
                shape = RoundedCornerShape(36.dp),
                onClick = {
                    if (isTextInputValid(textFieldValue.text)) {
                        onDismiss()
                        onDone(textFieldValue.text)
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}

@Composable
fun ConfirmationDialog(
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    confirmText: String = "Confirm",
    onCancel: () -> Unit = onDismiss
) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        BasicText(
            text = text,
            style = typography.xs.semiBold.center,
            modifier = Modifier
                .padding(all = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ChunkyButton(
                backgroundColor = Color.Transparent,
                text = cancelText,
                textStyle = typography.xs.semiBold,
                shape = RoundedCornerShape(36.dp),
                onClick = onCancel
            )

            ChunkyButton(
                backgroundColor = colorPalette.primaryContainer,
                text = confirmText,
                textStyle = typography.xs.semiBold.color(colorPalette.onPrimaryContainer),
                shape = RoundedCornerShape(36.dp),
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
inline fun DefaultDialog(
    noinline onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    crossinline content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = modifier
                .padding(all = 48.dp)
                .background(
                    color = LocalColorPalette.current.elevatedBackground,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            content = content
        )
    }
}


@Composable
inline fun <T> ValueSelectorDialog(
    noinline onDismiss: () -> Unit,
    title: String,
    selectedValue: T,
    values: List<T>,
    crossinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    crossinline valueText: (T) -> String = { it.toString() }
) {
    val typography = LocalTypography.current
    val colorPalette = LocalColorPalette.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 48.dp)
                .background(
                    color = LocalColorPalette.current.elevatedBackground,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 16.dp),
        ) {
            BasicText(
                text = title,
                style = typography.s.semiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                values.forEach { value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(bounded = true),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onDismiss()
                                    onValueSelected(value)
                                }
                            )
                            .padding(vertical = 8.dp, horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        if (selectedValue == value) {
                            Box(contentAlignment = Alignment.Center) {
                                Spacer(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            color = colorPalette.primaryContainer,
                                            shape = CircleShape
                                        )
                                )

                                Spacer(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = colorPalette.onPrimaryContainer,
                                            shape = CircleShape
                                        )
                                )
                            }

                        } else {
                            Spacer(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colorPalette.textDisabled,
                                        shape = CircleShape
                                    )
                            )
                        }

                        BasicText(
                            text = valueText(value),
                            style = typography.xs.medium
                        )
                    }
                }
            }

            BasicText(
                text = "Cancel",
                style = typography.xs.semiBold,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.End)
            )
        }
    }
}