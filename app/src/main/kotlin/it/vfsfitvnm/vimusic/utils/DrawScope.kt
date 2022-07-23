package it.vfsfitvnm.vimusic.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawCircle(
    color: Color,
    shadow: Shadow,
    radius: Float = size.minDimension / 2.0f,
    center: Offset = this.center,
    alpha: Float = 1.0f,
    style: PaintingStyle = PaintingStyle.Fill,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode
) = drawContext.canvas.nativeCanvas.drawCircle(
    center.x,
    center.y,
    radius,
    Paint().also {
        it.color = color
        it.alpha = alpha
        it.blendMode = blendMode
        it.colorFilter = colorFilter
        it.style = style
    }.asFrameworkPaint().also {
        it.setShadowLayer(
            shadow.blurRadius,
            shadow.offset.x,
            shadow.offset.y,
            shadow.color.toArgb()
        )
    }
)
