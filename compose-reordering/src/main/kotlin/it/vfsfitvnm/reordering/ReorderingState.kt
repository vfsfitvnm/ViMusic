package it.vfsfitvnm.reordering

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

class ReorderingState(
    internal val itemSizeProvider: ((Int) -> Int?)?,
    internal val coroutineScope: CoroutineScope,
    internal val lastIndex: Int,
    internal val areEquals: (Int, Int) -> Boolean,
    internal val orientation: Orientation,
    internal val onDragStart: () -> Unit,
    internal val onDragEnd: (Int, Int) -> Unit,
) {
    internal val offset: Animatable<Int, AnimationVector1D> = Animatable(0, Int.VectorConverter)

    internal var draggingIndex by mutableStateOf(-1)
    internal var reachedIndex by mutableStateOf(-1)
    internal var draggingItemSize by mutableStateOf(0)

    private val noTranslation = object : State<Int> {
        override val value = 0
    }

    @Composable
    internal fun translationFor(index: Int): State<Int> = when (draggingIndex) {
        -1 -> noTranslation
        index -> offset.asState()
        else -> animateIntAsState(
            when (index) {
                in (draggingIndex + 1)..reachedIndex -> -draggingItemSize
                in reachedIndex until draggingIndex -> draggingItemSize
                else -> 0
            }
        )
    }
}

@Composable
fun rememberReorderingState(
    items: List<Any>,
    onDragEnd: (Int, Int) -> Unit,
    onDragStart: () -> Unit = {},
    orientation: Orientation = Orientation.Vertical,
    itemSizeProvider: ((Int) -> Int?)? = null
): ReorderingState {
    val coroutineScope = rememberCoroutineScope()

    return remember(items) {
        ReorderingState(
            itemSizeProvider = itemSizeProvider,
            coroutineScope = coroutineScope,
            orientation = orientation,
            lastIndex = items.lastIndex,
            areEquals = { i, j -> items[i] == items[j] },
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
        )
    }
}
