package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.semiBold

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
            content = content
        )
    }
}

@Composable
fun RowScope.TabRowItem(
    tabPagerState: TabPagerState,
    index: Int,
    text: String,
) {
    val (colorPalette, typography) = LocalAppearance.current

    val alpha by animateFloatAsState(
        if (tabPagerState.transitioningIndex == index) {
            1f
        } else {
            0.4f
        }
    )

    val scale by animateFloatAsState(
        if (tabPagerState.transitioningIndex == index) {
            1f
        } else {
            0.9f
        }
    )

    BasicText(
        text = text,
        style = typography.s.semiBold.color(colorPalette.text).center,
        modifier = Modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    tabPagerState.animateScrollTo(index)
                }
            )
            .weight(1f)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .padding(vertical = 8.dp)
    )
}

inline val TabPagerState.transitioningIndex: Int
    get() = tempPageIndex ?: pageIndex