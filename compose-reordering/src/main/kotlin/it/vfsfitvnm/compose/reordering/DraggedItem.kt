package it.vfsfitvnm.compose.reordering

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex

fun Modifier.draggedItem(
    reorderingState: ReorderingState,
    index: Int
): Modifier = when (reorderingState.draggingIndex) {
    -1 -> this
    index -> offset {
        when (reorderingState.lazyListState.layoutInfo.orientation) {
            Orientation.Vertical -> IntOffset(0, reorderingState.offset.value)
            Orientation.Horizontal -> IntOffset(reorderingState.offset.value, 0)
        }
    }.zIndex(1f)
    else -> offset {
        val offset =  when (index) {
            in reorderingState.indexesToAnimate -> reorderingState.indexesToAnimate.getValue(index).value
            in (reorderingState.draggingIndex + 1)..reorderingState.reachedIndex -> -reorderingState.draggingItemSize
            in reorderingState.reachedIndex until reorderingState.draggingIndex -> reorderingState.draggingItemSize
            else -> 0
        }
        when (reorderingState.lazyListState.layoutInfo.orientation) {
            Orientation.Vertical -> IntOffset(0, offset)
            Orientation.Horizontal -> IntOffset(offset, 0)
        }
    }
}
