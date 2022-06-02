package it.vfsfitvnm.reordering

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.*

class ReorderingState(
    draggingIndexState: MutableState<Int>,
    reachedIndexState: MutableState<Int>,
    draggingItemSizeState: MutableState<Int>,
    internal val offset: Animatable<Int, AnimationVector1D>,
    internal val lastIndex: Int,
    internal val areEquals: (Int, Int) -> Boolean
) {
    internal var draggingIndex by draggingIndexState
    internal var reachedIndex by reachedIndexState
    internal var draggingItemSize by draggingItemSizeState

    @Composable
    internal fun translationFor(index: Int): State<Int> = when (draggingIndex) {
        -1 -> derivedStateOf { 0 }
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
fun rememberReorderingState(items: List<Any>): ReorderingState {
    val draggingIndexState = remember(items) {
        mutableStateOf(-1)
    }

    val reachedIndexState = remember(items) {
        mutableStateOf(-1)
    }

    val draggingItemHeightState = remember {
        mutableStateOf(0)
    }

    val offset = remember(items) {
        Animatable(0, Int.VectorConverter)
    }

    return remember(items) {
        ReorderingState(
            draggingIndexState = draggingIndexState,
            reachedIndexState = reachedIndexState,
            draggingItemSizeState = draggingItemHeightState,
            offset = offset,
            lastIndex = items.lastIndex,
            areEquals = { i, j ->
                items[i] == items[j]
            }
        )
    }
}
