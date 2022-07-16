package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@Composable
fun TabRow(
    tabPagerState: TabPagerState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val indicatorWidth = maxWidth.toPx() / tabPagerState.pageCount

                    var indicatorStart = tabPagerState.pageIndex * indicatorWidth

                    val targetPageIndex = tabPagerState.targetPageIndex

                    if (targetPageIndex != null && targetPageIndex != tabPagerState.pageIndex) {
                        val targetStart = targetPageIndex * indicatorWidth
                        indicatorStart += (targetStart - indicatorStart) * tabPagerState.progress
                    }

                    drawLine(
                        color = colorPalette.primaryContainer,
                        start = Offset(x = indicatorStart + 16.dp.toPx(), y = size.height),
                        end = Offset(x = indicatorStart + indicatorWidth - 16.dp.toPx(), y = size.height),
                        cap = StrokeCap.Round,
                        strokeWidth = 3.dp.toPx()
                    )
                },
            content = content,
        )
    }
}

inline val TabPagerState.transitioningIndex: Int
    get() = tempPageIndex ?: pageIndex