package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

data class TabPosition(
    val left: Int,
    val width: Int
)

@Composable
fun TabRow(
    tabPagerState: TabPagerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    var tabPositions by remember {
        mutableStateOf<List<TabPosition>?>(null)
    }

    val indicatorWidth by animateIntAsState(
        targetValue = (tabPositions?.getOrNull(tabPagerState.transitioningIndex)?.width ?: 0)
    )

    val indicatorStart by animateIntAsState(
        targetValue = (tabPositions?.getOrNull(tabPagerState.transitioningIndex)?.left ?: 0)
    )

    Layout(
        modifier = modifier
            .drawBehind {
                if (indicatorWidth == 0) return@drawBehind

                drawLine(
                    color = colorPalette.primaryContainer,
                    start = Offset(x = indicatorStart + 16.dp.toPx(), y = size.height),
                    end = Offset(
                        x = indicatorStart + indicatorWidth - 16.dp.toPx(),
                        y = size.height
                    ),
                    cap = StrokeCap.Round,
                    strokeWidth = 3.dp.toPx()
                )
            },
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }

        if (tabPositions == null) {
            var x = 0

            tabPositions = placeables.map { placeable ->
                TabPosition(
                    left = x,
                    width = placeable.width
                ).also {
                    x += placeable.width
                }
            }
        }

        layout(constraints.maxWidth, placeables.maxOf(Placeable::height)) {
            var x = 0
            placeables.fastForEach { placeable ->
                placeable.place(x = x, y = constraints.minHeight / 2)
                x += placeable.width
            }
        }
    }
}

inline val TabPagerState.transitioningIndex: Int
    get() = tempPageIndex ?: pageIndex