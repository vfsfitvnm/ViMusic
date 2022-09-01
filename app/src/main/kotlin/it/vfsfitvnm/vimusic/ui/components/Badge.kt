package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.badge(color: Color, isDisplayed: Boolean = true, radius: Dp = 4.dp) =
    if (isDisplayed) {
        drawWithContent {
            drawContent()
            drawCircle(
                color = color,
                center = Offset(x = size.width, y = 0.dp.toPx()),
                radius = radius.toPx()
            )
        }
    } else {
        this
    }
