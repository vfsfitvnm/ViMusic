package it.vfsfitvnm.vimusic.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.ranges.coerceAtMost


@Composable
@NonRestartableComposable
fun BottomSheet(
    lowerBound: Dp,
    upperBound: Dp,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 0.dp,
    elevation: Dp = 8.dp,
    shape: Shape = RectangleShape,
    handleOutsideInteractionsWhenExpanded: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    BottomSheet(
        state = rememberBottomSheetState(lowerBound, upperBound),
        modifier = modifier,
        peekHeight = peekHeight,
        elevation = elevation,
        shape = shape,
        handleOutsideInteractionsWhenExpanded = handleOutsideInteractionsWhenExpanded,
        interactionSource = interactionSource,
        collapsedContent = collapsedContent,
        content = content
    )
}

@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 0.dp,
    elevation: Dp = 8.dp,
    shape: Shape = RectangleShape,
    handleOutsideInteractionsWhenExpanded: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var lastOffset by remember {
        mutableStateOf(state.value)
    }

    BackHandler(enabled = !state.isCollapsed, onBack = state.collapse)

    Box {
        if (handleOutsideInteractionsWhenExpanded && !state.isCollapsed) {
            Spacer(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            state.collapse()
                        }
                    }
                    .draggable(
                        state = state,
                        onDragStarted = {
                            lastOffset = state.value
                        },
                        onDragStopped = { velocity ->
                            if (velocity.absoluteValue > 300 && lastOffset != state.value) {
                                if (lastOffset > state.value) {
                                    state.collapse()
                                } else {
                                    state.expand()
                                }
                            } else {
                                if (state.upperBound - state.value > state.value - state.lowerBound) {
                                    state.collapse()
                                } else {
                                    state.expand()
                                }
                            }
                        },
                        orientation = Orientation.Vertical
                    )
                    .drawBehind {
                        drawRect(color = Color.Black.copy(alpha = 0.5f * state.progress))
                    }
                    .fillMaxSize()
            )
        }

        Box(
            modifier = modifier
                .offset {
                    val y = (state.upperBound - state.value + peekHeight)
                        .roundToPx()
                        .coerceAtLeast(0)
                    IntOffset(x = 0, y = y)
                }
                .shadow(elevation = elevation, shape = shape)
                .clip(shape)
                .draggable(
                    state = state,
                    interactionSource = interactionSource,
                    onDragStarted = {
                        lastOffset = state.value
                    },
                    onDragStopped = { velocity ->
                        if (velocity.absoluteValue > 300 && lastOffset != state.value) {
                            if (lastOffset > state.value) {
                                state.collapse()
                            } else {
                                state.expand()
                            }
                        } else {
                            if (state.upperBound - state.value > state.value - state.lowerBound) {
                                state.collapse()
                            } else {
                                state.expand()
                            }
                        }
                    },
                    orientation = Orientation.Vertical
                )
                .clickable(
                    enabled = !state.isRunning && state.isCollapsed,
                    indication = null,
                    interactionSource = interactionSource
                        ?: remember { MutableInteractionSource() },
                    onClick = state.expand
                )
                .fillMaxSize()
        ) {
            if (!state.isCollapsed) {
                content()
            }

            collapsedContent()
        }
    }
}


@Stable
class BottomSheetState(
    draggableState: DraggableState,
    valueState: State<Dp>,
    isRunningState: State<Boolean>,
    isCollapsedState: State<Boolean>,
    isExpandedState: State<Boolean>,
    progressState: State<Float>,
    val lowerBound: Dp,
    val upperBound: Dp,
    val collapse: () -> Unit,
    val expand: () -> Unit,
) : DraggableState by draggableState {
    val value by valueState

    val isRunning by isRunningState

    val isCollapsed by isCollapsedState

    val isExpanded by isExpandedState

    val progress by progressState

    fun nestedScrollConnection(initialIsTopReached: Boolean = true): NestedScrollConnection {
        return object : NestedScrollConnection {
            var isTopReached = initialIsTopReached

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isExpanded && available.y < 0) {
                    isTopReached = false
                }

                if (isTopReached) {
                    dispatchRawDelta(available.y)
                    return available
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!isTopReached) {
                    isTopReached = consumed.y == 0f && available.y > 0
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isTopReached) {
                    coroutineScope {
                        if (available.y.absoluteValue > 1000) {
                            collapse()
                        } else {
                            if (upperBound - value > value - lowerBound) {
                                collapse()
                            } else {
                                expand()
                            }
                        }
                    }

                    return available
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isTopReached = false
                return super.onPostFling(consumed, available)
            }
        }
    }
}

@Composable
fun rememberBottomSheetState(lowerBound: Dp, upperBound: Dp): BottomSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var wasExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    val animatable = remember(lowerBound, upperBound) {
        Animatable(if (wasExpanded) upperBound else lowerBound, Dp.VectorConverter).also {
            it.updateBounds(lowerBound.coerceAtMost(upperBound), upperBound)
        }
    }

    LaunchedEffect(animatable.value == upperBound) {
        wasExpanded = animatable.value == upperBound
    }

    return remember(animatable, coroutineScope) {
        BottomSheetState(
            draggableState = DraggableState { delta ->
                coroutineScope.launch {
                    animatable.snapTo(animatable.value - density.run { delta.toDp() })
                }
            },
            valueState = animatable.asState(),
            lowerBound = lowerBound,
            upperBound = upperBound,
            isRunningState = derivedStateOf {
                animatable.isRunning
            },
            isCollapsedState = derivedStateOf {
                animatable.value == lowerBound
            },
            isExpandedState = derivedStateOf {
                animatable.value == upperBound
            },
            progressState = derivedStateOf {
                1f - (upperBound - animatable.value) / (upperBound - lowerBound)
            },
            collapse = {
                coroutineScope.launch {
                    animatable.animateTo(animatable.lowerBound!!)
                }
            },
            expand = {
                coroutineScope.launch {
                    animatable.animateTo(animatable.upperBound!!)
                }
            }
        )
    }
}
