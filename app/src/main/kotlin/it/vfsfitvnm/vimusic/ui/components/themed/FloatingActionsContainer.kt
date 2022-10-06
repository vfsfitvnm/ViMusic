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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.utils.isScrollingDown
import it.vfsfitvnm.vimusic.utils.isScrollingDownToIsFar
import it.vfsfitvnm.vimusic.utils.smoothScrollToTop
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val transitionState = remember {
        MutableTransitionState(false to false)
    }.apply { targetState = lazyGridState.isScrollingDownToIsFar() }

    FloatingActions(
        transitionState = transitionState,
        onScrollToTop = lazyGridState::smoothScrollToTop,
        iconId = iconId,
        onClick = onClick,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val transitionState = remember {
        MutableTransitionState(false to false)
    }.apply { targetState = lazyListState.isScrollingDownToIsFar() }

    FloatingActions(
        transitionState = transitionState,
        onScrollToTop = lazyListState::smoothScrollToTop,
        iconId = iconId,
        onClick = onClick,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActionsContainerWithScrollToTop(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val transitionState = remember {
        MutableTransitionState(false to false)
    }.apply { targetState = scrollState.isScrollingDown() to false }

    FloatingActions(
        transitionState = transitionState,
        iconId = iconId,
        onClick = onClick,
        modifier = modifier
    )
}

@ExperimentalAnimationApi
@Composable
fun BoxScope.FloatingActions(
    transitionState: MutableTransitionState<Pair<Boolean, Boolean>>,
    modifier: Modifier = Modifier,
    onScrollToTop: (suspend () -> Unit)? = null,
    iconId: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val transition = updateTransition(transitionState, "FloatingActionsContainer")

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp)
            .padding(LocalPlayerAwarePaddingValues.current)
    ) {
        onScrollToTop?.let {
            transition.AnimatedVisibility(
                visible = { it.first && it.second },
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
                    iconId = R.drawable.chevron_up,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
            }
        }

        iconId?.let {
            onClick?.let {
                transition.AnimatedVisibility(
                    visible = { it.first },
                    enter = slideInVertically(tween(500, 0)) { it },
                    exit = slideOutVertically(tween(500, 100)) { it },
                ) {
                    PrimaryButton(
                        iconId = iconId,
                        onClick = onClick,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}
