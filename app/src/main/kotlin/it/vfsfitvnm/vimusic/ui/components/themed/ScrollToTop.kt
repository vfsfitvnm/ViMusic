package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import kotlinx.coroutines.launch

@Composable
fun ScrollToTop(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val showScrollTopButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > lazyListState.layoutInfo.visibleItemsInfo.size
        }
    }

    ScrollToTop(
        isVisible = showScrollTopButton,
        onClick = lazyListState::smoothScrollToTop,
        modifier = modifier
    )
}

@Composable
private fun ScrollToTop(
    isVisible: Boolean,
    onClick: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .padding(all = 16.dp)
                .padding(LocalPlayerAwarePaddingValues.current)
                .clickable {
                    coroutineScope.launch {
                        onClick()
                    }
                }
                .size(32.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.chevron_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalAppearance.current.colorPalette.text),
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(180f)
                    .size(20.dp)
            )
        }
    }
}
