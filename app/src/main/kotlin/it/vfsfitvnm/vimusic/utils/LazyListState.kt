package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.lazy.LazyListState

suspend fun LazyListState.smoothScrollToTop() {
    if (firstVisibleItemIndex > layoutInfo.visibleItemsInfo.size) {
        scrollToItem(layoutInfo.visibleItemsInfo.size)
    }
    animateScrollToItem(0)
}
