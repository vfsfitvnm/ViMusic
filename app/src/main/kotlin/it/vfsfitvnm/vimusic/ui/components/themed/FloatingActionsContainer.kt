package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.utils.ScrollingInfo
import it.vfsfitvnm.vimusic.utils.scrollingInfo
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
    windowInsets: WindowInsets = LocalPlayerAwareWindowInsets.current
) {
    val transitionState = remember {
        MutableTransitionState<ScrollingInfo?>(ScrollingInfo())
    }.apply { targetState = if (visible) lazyGridState.scrollingInfo() else null }

    FloatingActions(
        transitionState = transitionState,
        onScrollToTop = lazyGridState::smoothScrollToTop,
        iconId = iconId,
        onClick = onClick,
        windowInsets = windowInsets,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
    windowInsets: WindowInsets = LocalPlayerAwareWindowInsets.current
) {
    val transitionState = remember {
        MutableTransitionState<ScrollingInfo?>(ScrollingInfo())
    }.apply { targetState = if (visible) lazyListState.scrollingInfo() else null }

    FloatingActions(
        transitionState = transitionState,
        onScrollToTop = lazyListState::smoothScrollToTop,
        iconId = iconId,
        onClick = onClick,
        windowInsets = windowInsets,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
    windowInsets: WindowInsets = LocalPlayerAwareWindowInsets.current
) {
    val transitionState = remember {
        MutableTransitionState<ScrollingInfo?>(ScrollingInfo())
    }.apply { targetState = if (visible) scrollState.scrollingInfo() else null }

    FloatingActions(
        transitionState = transitionState,
        iconId = iconId,
        onClick = onClick,
        windowInsets = windowInsets,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActions(
    transitionState: MutableTransitionState<ScrollingInfo?>,
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    onScrollToTop: (suspend () -> Unit)? = null,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val transition = updateTransition(transitionState, "")

    val bottomPaddingValues = windowInsets.only(WindowInsetsSides.Bottom).asPaddingValues()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp)
            .padding(windowInsets.only(WindowInsetsSides.End).asPaddingValues())
    ) {
        onScrollToTop?.let {
            transition.AnimatedVisibility(
                visible = { it?.isScrollingDown == false && it.isFar },
                enter = slideInVertically(tween(500, if (iconId == null) 0 else 100)) { it },
                exit = slideOutVertically(tween(500, 0)) { it },
            ) {
                val coroutineScope = rememberCoroutineScope()

                SecondaryButton(
                    onClick = {
                        coroutineScope.launch {
                            onScrollToTop()
                        }
                    },
                    enabled = transition.targetState?.isScrollingDown == false && transition.targetState?.isFar == true,
                    iconId = R.drawable.chevron_up,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .padding(bottomPaddingValues)
                )
            }
        }

        iconId?.let {
            onClick?.let {
                transition.AnimatedVisibility(
                    visible = { it?.isScrollingDown == false },
                    enter = slideInVertically(tween(500, 0)) { it },
                    exit = slideOutVertically(tween(500, 100)) { it },
                ) {
                    PrimaryButton(
                        iconId = iconId,
                        onClick = onClick,
                        enabled = transition.targetState?.isScrollingDown == false,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .padding(bottomPaddingValues)
                    )
                }
            }
        }
    }
}
