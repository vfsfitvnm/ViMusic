package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Stable
class TabPagerState(
    pageIndexState: MutableState<Int>,
    val pageCount: Int
) {
    var pageIndex by pageIndexState

    var tempPageIndex by mutableStateOf<Int?>(null)

    val animatable = Animatable(0f)

    val offset by animatable.asState()

    fun updateBounds(lowerBound: Float, upperBound: Float) {
        animatable.updateBounds(lowerBound, upperBound)
    }

    suspend fun animateScrollTo(newPageIndex: Int) {
        tempPageIndex = newPageIndex
        if (newPageIndex > pageIndex) {
            animatable.animateTo(
                animatable.upperBound!!, tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                )
            )
        } else if (newPageIndex < pageIndex) {
            animatable.animateTo(
                animatable.lowerBound!!, tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                )
            )
        }

        pageIndex = newPageIndex
        animatable.snapTo(0f)
        tempPageIndex = null
    }
}

@Composable
fun rememberTabPagerState(pageIndexState: MutableState<Int>, pageCount: Int): TabPagerState {
    return remember {
        TabPagerState(
            pageIndexState = pageIndexState,
            pageCount = pageCount,
        )
    }
}

@Composable
fun rememberTabPagerState(initialPageIndex: Int, pageCount: Int): TabPagerState {
    return remember {
        TabPagerState(
            pageIndexState = mutableStateOf(initialPageIndex),
            pageCount = pageCount
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalTabPager(
    state: TabPagerState,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val itemProvider = remember(state) {
        object : LazyLayoutItemProvider {
            override val itemCount = state.pageCount

            @Composable
            override fun Item(index: Int) = content(index)
        }
    }

    LazyLayout(
        itemProvider = itemProvider,
        modifier = modifier
            .clipToBounds()
            .pointerInput(state) {
                val velocityTracker = VelocityTracker()
                val decay = splineBasedDecay<Float>(this)

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        velocityTracker.addPointerInputChange(change)
                        coroutineScope.launch {
                            state.animatable.snapTo(state.offset - dragAmount)
                        }
                    },
                    onDragEnd = {
                        val velocity = -velocityTracker.calculateVelocity().x
                        val initialTargetValue =
                            decay.calculateTargetValue(state.offset, velocity)

                        velocityTracker.resetTracking()

                        coroutineScope.launch {
                            val isEnough = initialTargetValue.absoluteValue > size.width / 2
                            if (initialTargetValue > 0) {
                                state.animatable.animateTo(
                                    targetValue = if (isEnough) size.width.toFloat() else 0f,
                                    initialVelocity = if (isEnough) velocity else 0f
                                )
                                if (isEnough) {
                                    state.pageIndex = state.pageIndex
                                        .plus(1)
                                        .coerceAtMost(state.pageCount - 1)
                                    state.animatable.snapTo(0f)
                                }
                            } else {
                                state.animatable.animateTo(
                                    targetValue = if (isEnough) -size.width.toFloat() else 0f,
                                    initialVelocity = if (isEnough) velocity else 0f
                                )
                                if (isEnough) {
                                    state.pageIndex = state.pageIndex
                                        .minus(1)
                                        .coerceAtLeast(0)
                                    state.animatable.snapTo(0f)
                                }
                            }
                        }
                    }
                )
            }
    ) { constraints ->
        val previousPlaceable = state.offset.takeIf { it < 0 }?.let {
            (state.tempPageIndex ?: (state.pageIndex - 1)).takeIf { it >= 0 }?.let { index ->
                measure(index, constraints).firstOrNull()
            }
        }
        val placeable = measure(state.pageIndex, constraints).first()

        val nextPlaceable = state.offset.takeIf { it > 0 }?.let {
            (state.tempPageIndex ?: (state.pageIndex + 1)).takeIf { it < state.pageCount }
                ?.let { index ->
                    measure(index, constraints).firstOrNull()
                }
        }

        state.updateBounds(
            lowerBound = if (state.pageIndex == 0) 0f else -constraints.maxWidth.toFloat(),
            upperBound = if (state.pageIndex == state.pageCount - 1) 0f else constraints.maxWidth.toFloat()
        )

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            previousPlaceable?.let {
                previousPlaceable.place(x = -state.offset.toInt() - constraints.maxWidth, y = 0)
                placeable.place(x = -state.offset.toInt(), y = 0)
            } ?: nextPlaceable?.let {
                placeable.place(x = -state.offset.toInt(), y = 0)
                nextPlaceable.place(x = -state.offset.toInt() + constraints.maxWidth, y = 0)
            } ?: placeable.place(x = -state.offset.toInt(), y = 0)
        }
    }
}