package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong

@Composable
fun SeekBar(
    value: Long,
    minimumValue: Long,
    maximumValue: Long,
    onDragStart: (Long) -> Unit,
    onDrag: (Long) -> Unit,
    onDragEnd: () -> Unit,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    barHeight: Dp = 3.5.dp,
    scrubberColor: Color = color,
    scrubberRadius: Dp = 6.dp,
    shape: Shape = RectangleShape,
) {
    Box(
        modifier = modifier
            .pointerInput(minimumValue, maximumValue) {
                if (maximumValue < minimumValue) return@pointerInput

                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, delta ->
                        onDrag((delta / size.width * (maximumValue - minimumValue)).roundToLong())
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
            .pointerInput(minimumValue, maximumValue) {
                if (maximumValue < minimumValue) return@pointerInput

                detectTapGestures(
                    onPress = { offset ->
                        onDragStart((offset.x / size.width * (maximumValue - minimumValue) + minimumValue).roundToLong())
                    },
                    onTap = {
                        onDragEnd()
                    }
                )
            }
            .padding(horizontal = scrubberRadius)
            .drawWithContent {
                drawContent()

                val scrubberPosition = if (maximumValue < minimumValue) {
                    0f
                } else {
                    (value.toFloat() - minimumValue) / (maximumValue - minimumValue) * size.width
                }

                drawCircle(
                    color = scrubberColor,
                    radius = scrubberRadius.toPx(),
                    center = center.copy(x = scrubberPosition)
                )
            }
    ) {
        Spacer(
            modifier = Modifier
                .height(barHeight)
                .fillMaxWidth()
                .background(color = backgroundColor, shape = shape)
                .align(Alignment.Center)
        )

        Spacer(
            modifier = Modifier
                .height(barHeight)
                .fillMaxWidth((value.toFloat() - minimumValue) / (maximumValue - minimumValue))
                .background(color = color, shape = shape)
        )
    }
}
