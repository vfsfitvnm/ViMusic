package it.vfsfitvnm.vimusic.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.italic
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.Outcome

@Composable
fun <T> OutcomeItem(
    outcome: Outcome<T>,
    onInitialize: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = onInitialize,
    onUninitialized: @Composable () -> Unit = {
        onInitialize?.let {
            SideEffect(it)
        }
    },
    onLoading: @Composable () -> Unit = {},
    onError: @Composable (Outcome.Error) -> Unit = {
        Error(
            error = it,
            onRetry = onRetry,
        )
    },
    onSuccess: @Composable (T) -> Unit
) {
    when (outcome) {
        is Outcome.Initial -> onUninitialized()
        is Outcome.Loading -> onLoading()
        is Outcome.Error -> onError(outcome)
        is Outcome.Recovered -> onError(outcome.error)
        is Outcome.Success -> onSuccess(outcome.value)
    }
}

@Composable
fun Error(
    error: Outcome.Error,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.alert_circle),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFFFC5F5F)),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(48.dp)
        )
        BasicText(
            text = when (error) {
                is Outcome.Error.Network -> "Couldn't reach the Internet"
                is Outcome.Error.Unhandled -> (error.throwable.message ?: error.throwable.toString())
            },
            style = LocalTypography.current.xxs.medium.secondary,
        )

        onRetry?.let { retry ->
            BasicText(
                text = "Retry",
                style = LocalTypography.current.xxs.medium,
                modifier = Modifier
                    .clickable(onClick = retry)
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
            )
        }

    }
}

@Composable
fun Message(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.alert_circle
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalColorPalette.current.darkGray),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(36.dp)
        )
        BasicText(
            text = text,
            style = LocalTypography.current.xs.medium.secondary.italic,
        )
    }
}