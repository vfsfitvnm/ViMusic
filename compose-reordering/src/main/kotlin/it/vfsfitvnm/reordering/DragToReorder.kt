package it.vfsfitvnm.reordering

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import kotlin.reflect.KSuspendFunction5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun Modifier.dragToReorder(
    reorderingState: ReorderingState,
    index: Int,
    orientation: Orientation,
    function: KSuspendFunction5<PointerInputScope, (Offset) -> Unit, () -> Unit, () -> Unit, (change: PointerInputChange, dragAmount: Offset) -> Unit, Unit>,
): Modifier = pointerInput(reorderingState) {
//        require(index in 0..reorderingState.lastIndex)

    var previousItemSize = 0
    var nextItemSize = 0

    function(
        this,
        {
            reorderingState.onDragStart.invoke()
            reorderingState.draggingIndex = index
            reorderingState.reachedIndex = index
            reorderingState.draggingItemSize = reorderingState.itemSizeProvider?.invoke(index) ?: when (orientation) {
                Orientation.Vertical -> size.height
                Orientation.Horizontal -> size.width
            }

            nextItemSize = reorderingState.draggingItemSize
            previousItemSize = -reorderingState.draggingItemSize

            reorderingState.offset.updateBounds(
                lowerBound = -index * reorderingState.draggingItemSize,
                upperBound = (reorderingState.lastIndex - index) * reorderingState.draggingItemSize
            )
        },
        {
            reorderingState.coroutineScope.launch {
                reorderingState.offset.animateTo((previousItemSize + nextItemSize) / 2)

                withContext(Dispatchers.Main) {
                    reorderingState.onDragEnd.invoke(index, reorderingState.reachedIndex)
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
                }
            } else if (targetOffset < previousItemSize) {
                if (reorderingState.reachedIndex > 0) {
                    reorderingState.reachedIndex -= 1
                    previousItemSize -= reorderingState.draggingItemSize
                    nextItemSize -= reorderingState.draggingItemSize
                }
            }

            reorderingState.coroutineScope.launch {
                reorderingState.offset.snapTo(targetOffset)
            }
        },
    )
}

fun Modifier.dragToReorder(
    reorderingState: ReorderingState,
    index: Int,
    orientation: Orientation,
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = orientation,
    function = PointerInputScope::detectDragGestures,
)

fun Modifier.verticalDragToReorder(
    reorderingState: ReorderingState,
    index: Int,
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = Orientation.Vertical,
)

fun Modifier.horizontalDragToReorder(
    reorderingState: ReorderingState,
    index: Int,
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = Orientation.Horizontal,
)

fun Modifier.dragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    index: Int,
    orientation: Orientation,
): Modifier = dragToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = orientation,
    function = PointerInputScope::detectDragGesturesAfterLongPress,
)

fun Modifier.verticalDragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    index: Int,
): Modifier = dragAfterLongPressToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = Orientation.Vertical,
)

fun Modifier.horizontalDragAfterLongPressToReorder(
    reorderingState: ReorderingState,
    index: Int
): Modifier = dragAfterLongPressToReorder(
    reorderingState = reorderingState,
    index = index,
    orientation = Orientation.Horizontal,
)
