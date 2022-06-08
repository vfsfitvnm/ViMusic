package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
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
    modifier: Modifier,
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

                forEachGesture {
                    awaitPointerEventScope {
                        val position = awaitFirstDown(requireUnconsumed = false)
                        onDragStart((position.position.x / size.width * (maximumValue - minimumValue)).roundToLong())

                        position.consume()

                        if (awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull()
                                ?.changedToUp() == true
                        ) {
                            onDragEnd()
                        }
                    }
                }
            }
            .padding(horizontal = scrubberRadius)
            .drawWithContent {
                drawContent()

                val scrubberPosition = if (maximumValue < minimumValue) {
                    0f
                } else {
                    (value.toFloat() + minimumValue) / (maximumValue - minimumValue) * size.width
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
                .fillMaxWidth((value.toFloat() + minimumValue) / (maximumValue - minimumValue))
                .background(color = color, shape = shape)
        )
    }
}
