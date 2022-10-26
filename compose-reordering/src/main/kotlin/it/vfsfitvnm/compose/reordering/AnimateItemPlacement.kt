package it.vfsfitvnm.compose.reordering

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier

context(LazyItemScope)
@ExperimentalFoundationApi
fun Modifier.animateItemPlacement(reorderingState: ReorderingState) =
    if (reorderingState.draggingIndex == -1) animateItemPlacement() else this
