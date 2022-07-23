package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.R

@Composable
fun LoadingOrError(
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    loadingContent: @Composable ColumnScope.() -> Unit
) {
    Box {
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .alpha(if (errorMessage == null) 1f else 0f)
                .shimmer(),
            content = loadingContent
        )

        errorMessage?.let {
            TextCard(
                icon = R.drawable.alert_circle,
                onClick = onRetry,
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Title(text = onRetry?.let { "Tap to retry" } ?: "Error")
                Text(text = "An error has occurred:\n$errorMessage")
            }
        }
    }
}
