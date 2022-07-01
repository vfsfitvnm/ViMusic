package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.secondary


@Composable
fun LyricsView(
    lyrics: String?,
    onInitialize: () -> Unit,
    onSearchOnline: () -> Unit,
    onLyricsUpdate: (String) -> Unit,
    nestedScrollConnectionProvider: () -> NestedScrollConnection,
) {
    val typography = LocalTypography.current

    var isEditingLyrics by remember {
        mutableStateOf(false)
    }

    if (isEditingLyrics) {
        TextFieldDialog(
            hintText = "Enter the lyrics",
            initialTextInput = lyrics ?: "",
            singleLine = false,
            maxLines = 10,
            isTextInputValid = { true },
            onDismiss = {
                isEditingLyrics = false
            },
            onDone = onLyricsUpdate
        )
    }

    if (lyrics != null ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 64.dp)
                .nestedScroll(remember { nestedScrollConnectionProvider() })
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(horizontal = 48.dp)
        ) {
            if (lyrics.isEmpty()) {
                TextCard(
                    icon = R.drawable.sad
                ) {
                    Title(text = "No results found")
                    Text(text = "Please try a different query or category.")
                }
            } else {
                BasicText(
                    text = lyrics,
                    style = typography.xs.center,
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 32.dp)
            ) {
                BasicText(
                    text = "Search online",
                    style = typography.xs.secondary.copy(textDecoration = TextDecoration.Underline),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onSearchOnline
                        )
                        .padding(horizontal = 8.dp)
                )

                BasicText(
                    text = "Edit",
                    style = typography.xs.secondary.copy(textDecoration = TextDecoration.Underline),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isEditingLyrics = true
                        }
                        .padding(horizontal = 8.dp)
                )
            }
        }
    } else {
        SideEffect(onInitialize)

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .shimmer()
        ) {
            repeat(16) { index ->
                TextPlaceholder(
                    modifier = Modifier
                        .alpha(1f - index * 0.05f)
                )
            }
        }
    }
}
