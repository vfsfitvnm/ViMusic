package it.vfsfitvnm.reordering

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex

fun Modifier.draggedItem(
    reorderingState: ReorderingState,
    index: Int
): Modifier = composed {
    val translation by reorderingState.translationFor(index)

    offset {
        when (reorderingState.lazyListState.layoutInfo.orientation) {
            Orientation.Vertical -> IntOffset(0, translation)
            Orientation.Horizontal -> IntOffset(translation, 0)
        }
    }
    .zIndex(if (reorderingState.draggingIndex == index) 1f else 0f)
}
