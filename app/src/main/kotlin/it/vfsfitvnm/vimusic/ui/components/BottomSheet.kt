package it.vfsfitvnm.vimusic.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.absoluteValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 0.dp,
    elevation: Dp = 8.dp,
    onSwiped: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .offset {
                val y = (state.upperBound - state.value + peekHeight)
                    .roundToPx()
                    .coerceAtLeast(0)
                IntOffset(x = 0, y = y)
            }
            .shadow(elevation = elevation)
            .pointerInput(state) {
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
                    onDragCancel = {
                        velocityTracker.resetTracking()
                        state.snapTo(initialValue)
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y.absoluteValue
                        velocityTracker.resetTracking()

                        if (velocity.absoluteValue > 300 && initialValue != state.value) {
                            when (initialValue) {
                                state.upperBound -> state.collapse()
                                state.collapsedBound -> if (initialValue > state.value && onSwiped != null) {
                                    state.swipe()
                                    onSwiped.invoke()
                                } else {
                                    state.expand()
                                }
                            }
                        } else {
                            val l0 = state.lowerBound
                            val l1 = (state.collapsedBound - state.lowerBound) / 2
                            val l2 = (state.upperBound - state.collapsedBound) / 2
                            val l3 = state.upperBound

                            when (state.value) {
                                in l0..l1 -> {
                                    if (onSwiped != null) {
                                        state.swipe()
                                        onSwiped.invoke()
                                    } else {
                                        state.collapse()
                                    }
                                }
                                in l1..l2 -> state.collapse()
                                in l2..l3 -> state.expand()
                                else -> {}
                            }
                        }

                    }
                )
            }
            .fillMaxSize()
    ) {
        if (!state.isCollapsed) {
            BackHandler(onBack = state::collapseSoft)
            content()
        }

        if (!state.isExpanded && (onSwiped == null || !state.isDismissed)) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - (state.progress * 16).coerceAtMost(1f)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                        onClick = state::expandSoft
                    )
                    .fillMaxWidth()
                    .height(state.collapsedBound),
                content = collapsedContent
            )
        }
    }
}

@Stable
class BottomSheetState(
    draggableState: DraggableState,
    private val coroutineScope: CoroutineScope,
    private val animatable: Animatable<Dp, AnimationVector1D>,
    private val onWasExpandedChanged: (Boolean) -> Unit,
    val collapsedBound: Dp,
) : DraggableState by draggableState {
    val lowerBound: Dp
        get() = animatable.lowerBound!!

    val upperBound: Dp
        get() = animatable.upperBound!!

    val value by animatable.asState()

    val isDismissed by derivedStateOf {
        value == animatable.lowerBound!!
    }

    val isCollapsed by derivedStateOf {
        value == collapsedBound
    }

    val isExpanded by derivedStateOf {
        value == animatable.upperBound
    }

    val progress by derivedStateOf {
        1f - (animatable.upperBound!! - animatable.value) / (animatable.upperBound!! - collapsedBound)
    }

    private fun collapse(animationSpec: AnimationSpec<Dp>) {
        onWasExpandedChanged(false)
        coroutineScope.launch {
            animatable.animateTo(collapsedBound, animationSpec)
        }
    }

    fun expand(animationSpec: AnimationSpec<Dp>) {
        onWasExpandedChanged(true)
        coroutineScope.launch {
            animatable.animateTo(animatable.upperBound!!, animationSpec)
        }
    }

    fun collapse() {
        collapse(SpringSpec())
    }

    fun expand() {
        expand(SpringSpec())
    }

    fun collapseSoft() {
        collapse(tween(300))
    }

    fun expandSoft() {
        expand(tween(300))
    }

    fun swipe() {
        onWasExpandedChanged(false)
        coroutineScope.launch {
            animatable.animateTo(animatable.lowerBound!!)
        }
    }

    fun snapTo(value: Dp) {
        coroutineScope.launch {
            animatable.snapTo(value)
        }
    }

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
                            if (animatable.upperBound!! - value > value - collapsedBound) {
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
fun rememberBottomSheetState(
    lowerBound: Dp,
    upperBound: Dp,
    collapsedBound: Dp = lowerBound,
    isExpanded: Boolean = false
): BottomSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var wasExpanded by rememberSaveable {
        mutableStateOf(isExpanded)
    }

    return remember(lowerBound, upperBound, collapsedBound, coroutineScope) {
        val animatable =
            Animatable(if (wasExpanded) upperBound else lowerBound, Dp.VectorConverter).also {
                it.updateBounds(lowerBound.coerceAtMost(upperBound), upperBound)
            }

        BottomSheetState(
            draggableState = DraggableState { delta ->
                coroutineScope.launch {
                    animatable.snapTo(animatable.value - with(density) { delta.toDp() })
                }
            },
            onWasExpandedChanged = {
                wasExpanded = it
            },
            coroutineScope = coroutineScope,
            animatable = animatable,
            collapsedBound = collapsedBound
        )
    }
}
