package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@Composable
fun Pager(
    selectedIndex: Int,
    onSelectedIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    alignment: Alignment = Alignment.Center,
    transformer: PagerTransformer = PagerTransformer.Default,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val velocityTracker = remember {
        VelocityTracker()
    }

    val state = remember {
        Animatable(0f)
    }

    var steps by remember {
        mutableStateOf(emptyList<Int>())
    }

    Layout(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                val function = when (orientation) {
                    Orientation.Vertical -> ::detectVerticalDragGestures
                    Orientation.Horizontal -> ::detectHorizontalDragGestures
                }

                function(
                    {},
                    {
                        val velocity = -velocityTracker.calculateVelocity().x
                        val initialTargetValue = splineBasedDecay<Float>(this).calculateTargetValue(
                            state.value,
                            velocity
                        )

                        velocityTracker.resetTracking()

                        val (targetValue, newSelectedIndex) = run {
                            for (i in 1..steps.lastIndex) {
                                val current = steps[i]
                                val previous = steps[i - 1]

                                val currentDelta = current - initialTargetValue
                                val previousDelta = initialTargetValue - previous

                                return@run when {
                                    currentDelta >= 0 && previousDelta > 0 -> if (currentDelta < previousDelta) {
                                        current to i
                                    } else {
                                        previous to i - 1
                                    }
                                    previousDelta <= 0 -> previous to i - 1
                                    else -> continue
                                }
                            }

                            steps.last() to steps.lastIndex
                        }

                        coroutineScope.launch {
                            state.animateTo(
                                targetValue = targetValue.toFloat(),
                                initialVelocity = velocity,
                            )
                        }

                        onSelectedIndex(newSelectedIndex)
                    },
                    {},
                    { change, dragAmount ->
                        coroutineScope.launch {
                            state.snapTo(state.value - dragAmount)
                        }

                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        change.consume()
                    },
                )
            },
        content = content
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map {
            it.measure(childConstraints)
        }

        var acc = 0
        steps = placeables.map {
            val dim = when (orientation) {
                Orientation.Horizontal -> it.width
                Orientation.Vertical -> it.height
            }
            val step = acc + dim / 2
            acc += dim
            step
        }.also {
            if (steps.isEmpty()) {
                coroutineScope.launch {
                    state.animateTo(it[selectedIndex].toFloat())
                }
            }
        }

        state.updateBounds(
            lowerBound = steps.first().toFloat(),
            upperBound = steps.last().toFloat()
        )

        val layoutDimension = IntSize(
            width = if (constraints.minWidth > 0 || placeables.isEmpty()) {
                constraints.minWidth
            } else {
                placeables.maxOf {
                    it.width
                }
            },
            height = if (constraints.minHeight > 0 || placeables.isEmpty()) {
                constraints.minHeight
            } else {
                placeables.maxOf {
                    it.height
                }
            }
        )

        val center = when (orientation) {
            Orientation.Horizontal -> layoutDimension.center.x
            Orientation.Vertical -> layoutDimension.center.y
        }

        layout(
            width = layoutDimension.width,
            height = layoutDimension.height
        ) {
            var position = center - state.value.toInt()

            for (placeable in placeables) {
                val otherPosition = alignment.align(
                    size = IntSize(
                        width = placeable.width,
                        height = placeable.height
                    ),
                    space = layoutDimension,
                    layoutDirection = layoutDirection
                ).let {
                    when (orientation) {
                        Orientation.Horizontal -> it.y
                        Orientation.Vertical -> it.x
                    }
                }

                val placeablePosition = when (orientation) {
                    Orientation.Horizontal -> IntOffset(position, otherPosition)
                    Orientation.Vertical -> IntOffset(otherPosition, position)
                }

                placeable.placeWithLayer(position = placeablePosition) {
                    with(transformer) {
                        val size = when (orientation) {
                            Orientation.Horizontal -> placeable.width
                            Orientation.Vertical -> placeable.height
                        }.toFloat()
                        val offset = (center - (position + size / 2)).absoluteValue / size
                        apply(distance = offset)
                    }
                }

                position += when (orientation) {
                    Orientation.Horizontal -> placeable.width
                    Orientation.Vertical -> placeable.height
                }
            }
        }
    }
}

// Cannot inline: https://issuetracker.google.com/issues/204897513
@Composable
fun <T> ItemPager(
    items: List<T>,
    selectedIndex: Int,
    onSelectedIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    alignment: Alignment = Alignment.Center,
    transformer: PagerTransformer = PagerTransformer.Default,
    content: @Composable (item: T) -> Unit
) {
    Pager(
        modifier = modifier,
        selectedIndex = selectedIndex,
        onSelectedIndex = onSelectedIndex,
        orientation = orientation,
        alignment = alignment,
        transformer = transformer,
    ) {
        for (item in items) {
            content(item)
        }
    }
}

// Cannot inline: https://issuetracker.google.com/issues/204897513
@Composable
fun <T> ItemPager(
    items: List<T>,
    selectedValue: T,
    onSelectedValue: (T) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    alignment: Alignment = Alignment.Center,
    transformer: PagerTransformer = PagerTransformer.Default,
    content: @Composable (item: T) -> Unit
) {
    Pager(
        modifier = modifier,
        selectedIndex = items.indexOf(selectedValue).coerceAtLeast(0),
        onSelectedIndex = {
            onSelectedValue(items[it])
        },
        orientation = orientation,
        alignment = alignment,
        transformer = transformer,
    ) {
        for (item in items) {
            content(item)
        }
    }
}

// Cannot inline: https://issuetracker.google.com/issues/204897513
@Composable
fun <T : Enum<T>> EnumPager(
    value: T,
    onSelectedValue: (T) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    alignment: Alignment = Alignment.Center,
    transformer: PagerTransformer = PagerTransformer.Default,
    content: @Composable (item: T) -> Unit
) {
    val items = remember {
        value.declaringJavaClass.enumConstants!!
    }

    Pager(
        modifier = modifier,
        selectedIndex = value.ordinal,
        onSelectedIndex = {
            onSelectedValue(items[it])
        },
        orientation = orientation,
        alignment = alignment,
        transformer = transformer,
    ) {
        for (item in items) {
            content(item)
        }
    }
}

@Immutable
fun interface PagerTransformer {
    fun GraphicsLayerScope.apply(distance: Float)

    companion object {
        @Stable
        val Empty = PagerTransformer {}

        @Stable
        val Default = PagerTransformer {
            val value = 1f - it.coerceIn(0f, 1f)
            lerp(start = 0.85f, stop = 1f, fraction = value).also { scale ->
                scaleX = scale
                scaleY = scale
            }

            alpha = lerp(start = 0.5f, stop = 1f, fraction = value)
        }
    }
}
