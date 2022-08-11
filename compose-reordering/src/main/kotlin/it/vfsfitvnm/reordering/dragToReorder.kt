package it.vfsfitvnm.reordering

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.reflect.KSuspendFunction5

private fun Modifier.dragToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    orientation: Orientation,
    function: KSuspendFunction5<PointerInputScope, (Offset) -> Unit, () -> Unit, () -> Unit, (change: PointerInputChange, dragAmount: Offset) -> Unit, Unit>,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val translation by reorderingState.translationFor(index)

    pointerInput(reorderingState) {
//        require(index in 0..reorderingState.lastIndex)

        var previousItemSize = 0
        var nextItemSize = 0


        function(
            this,
            {
                onDragStart?.invoke()
                reorderingState.draggingIndex = index
                reorderingState.reachedIndex = index
                reorderingState.draggingItemSize = size.height

                nextItemSize = reorderingState.draggingItemSize
                previousItemSize = -reorderingState.draggingItemSize

                reorderingState.offset.updateBounds(
                    lowerBound = -index * reorderingState.draggingItemSize,
                    upperBound = (reorderingState.lastIndex - index) * reorderingState.draggingItemSize
                )
            },
            {
                coroutineScope.launch {
                    reorderingState.offset.animateTo((previousItemSize + nextItemSize) / 2)

                    withContext(Dispatchers.Main) {
                        onDragEnd?.invoke(reorderingState.reachedIndex)
                    }

                    if (reorderingState.areEquals(
                            reorderingState.draggingIndex,
                            reorderingState.reachedIndex
                        )
                    ) {
                        reorderingState.draggingIndex = -1
                        reorderingState.reachedIndex = -1
                        reorderingState.draggingItemSize = 0
                        reorderingState.offset.snapTo(0)
                    }
                }
            },
            {},
            { _, offset ->
                val delta = when (orientation) {
                    Orientation.Vertical -> offset.y
                    Orientation.Horizontal -> offset.x
                }.roundToInt()

                val targetOffset = reorderingState.offset.value + delta

                if (targetOffset > nextItemSize) {
                    if (reorderingState.reachedIndex < reorderingState.lastIndex) {
                        reorderingState.reachedIndex += 1
                        nextItemSize += reorderingState.draggingItemSize
                        previousItemSize += reorderingState.draggingItemSize
                        onMove?.invoke()
                        Log.d("TAG", reorderingState.reachedIndex.toString()) // next
                    }
                } else if (targetOffset < previousItemSize) {
                    if (reorderingState.reachedIndex > 0) {
                        reorderingState.reachedIndex -= 1
                        previousItemSize -= (reorderingState.draggingItemSize)
                        nextItemSize -= (reorderingState.draggingItemSize)
                        onMove?.invoke()
                        while( reorderingState.reachedIndex == lazyListState.firstVisibleItemIndex) {
                            Log.d("TAG", "object is at the top") // TODO add the fact that the object is going upper
                        }
                    }
                }

                coroutineScope.launch {
                    reorderingState.offset.snapTo(targetOffset)
                }
            },
        )
    }
        .offset {
            when (orientation) {
                Orientation.Vertical -> IntOffset(0, translation)
                Orientation.Horizontal -> IntOffset(translation, 0)
            }
        }
        .zIndex(if (reorderingState.draggingIndex == index) 1f else 0f)
}

fun Modifier.dragToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    orientation: Orientation,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    lazyListState = lazyListState,
    index = index,
    orientation = orientation,
    function = PointerInputScope::detectDragGestures,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)

fun Modifier.verticalDragToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    lazyListState = lazyListState,
    index = index,
    orientation = Orientation.Vertical,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)

fun Modifier.horizontalDragToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    lazyListState,
    index = index,
    orientation = Orientation.Horizontal,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)

fun Modifier.dragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    orientation: Orientation,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    lazyListState = lazyListState,
    index = index,
    orientation = orientation,
    function = PointerInputScope::detectDragGesturesAfterLongPress,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)

fun Modifier.verticalDragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragAfterLongPressToReorder(
    reorderingState = reorderingState,
    lazyListState = lazyListState,
    index = index,
    orientation = Orientation.Vertical,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)

fun Modifier.horizontalDragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    lazyListState: LazyListState,
    index: Int,
    onDragStart: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onDragEnd: ((Int) -> Unit)? = null
): Modifier = dragAfterLongPressToReorder(
    reorderingState = reorderingState,
    lazyListState = lazyListState,
    index = index,
    orientation = Orientation.Horizontal,
    onDragStart = onDragStart,
    onMove = onMove,
    onDragEnd = onDragEnd,
)
