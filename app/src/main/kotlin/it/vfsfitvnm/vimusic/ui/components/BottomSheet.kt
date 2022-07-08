package it.vfsfitvnm.vimusic.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 0.dp,
    elevation: Dp = 8.dp,
    shape: Shape = RectangleShape,
    handleOutsideInteractionsWhenExpanded: Boolean = false,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box {
        if (handleOutsideInteractionsWhenExpanded && !state.isCollapsed) {
            Spacer(
                modifier = Modifier
                    .pointerInput(state) {
                        detectTapGestures {
                            state.collapse()
                        }
                    }
                    .draggableBottomSheet(state)
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
                .draggableBottomSheet(state)
                .pointerInput(state) {
                    if (!state.isRunning && state.isCollapsed) {
                        detectTapGestures {
                            state.expand()
                        }
                    }
                }
                .fillMaxSize()
        ) {
            if (!state.isCollapsed) {
                BackHandler(onBack = state.collapse)
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

private fun Modifier.draggableBottomSheet(state: BottomSheetState) = pointerInput(state) {
    var initialValue = 0.dp
    val velocityTracker = VelocityTracker()

    detectVerticalDragGestures(
        onDragStart = {
            initialValue = state.value
        },
        onVerticalDrag = { change, dragAmount ->
            velocityTracker.addPointerInputChange(change)
            state.dispatchRawDelta(dragAmount)
        },
        onDragEnd = {
            val velocity = velocityTracker.calculateVelocity().y.absoluteValue
            velocityTracker.resetTracking()

            if (velocity.absoluteValue > 300 && initialValue != state.value) {
                if (initialValue > state.value) {
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
        }
    )
}