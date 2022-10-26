@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package it.vfsfitvnm.compose.reordering

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReorderingLazyColumn(
    reorderingState: ReorderingState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    ReorderingLazyList(
        modifier = modifier,
        reorderingState = reorderingState,
        contentPadding = contentPadding,
        flingBehavior = flingBehavior,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        isVertical = true,
        reverseLayout = reverseLayout,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}
