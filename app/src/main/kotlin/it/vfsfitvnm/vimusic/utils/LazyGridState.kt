package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

suspend fun LazyGridState.smoothScrollToTop() {
    if (firstVisibleItemIndex > layoutInfo.visibleItemsInfo.size) {
        scrollToItem(layoutInfo.visibleItemsInfo.size)
    }
    animateScrollToItem(0)
}

@Composable
fun LazyGridState.isScrollingDownToIsFar(): Pair<Boolean, Boolean> {
    var previousIndex by remember(this) {
        mutableStateOf(firstVisibleItemIndex)
    }

    var previousScrollOffset by remember(this) {
        mutableStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also  {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            } to (firstVisibleItemIndex > layoutInfo.visibleItemsInfo.size)
        }
    }.value
}
