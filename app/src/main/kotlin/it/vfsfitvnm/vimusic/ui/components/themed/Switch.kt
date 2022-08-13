package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.drawCircle

@Composable
fun Switch(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    val (colorPalette) = LocalAppearance.current

    val transition = updateTransition(targetState = isChecked, label = null)

    val backgroundColor by transition.animateColor(label = "") {
        if (it) colorPalette.accent else colorPalette.background1
    }

    val color by transition.animateColor(label = "") {
        if (it) colorPalette.onAccent else colorPalette.textDisabled
    }

    val offset by transition.animateDp(label = "") {
        if (it) 36.dp else 12.dp
    }

    Canvas(
        modifier = modifier
            .size(width = 48.dp, height = 24.dp)
    ) {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx()),
        )

        drawCircle(
            color = color,
            radius = 8.dp.toPx(),
            center = size.center.copy(x = offset.toPx()),
            shadow = Shadow(
                color = Color.Black.copy(alpha = if (isChecked) 0.4f else 0.1f),
                blurRadius = 8.dp.toPx(),
                offset = Offset(x = -1.dp.toPx(), y = 1.dp.toPx())
            )
        )
    }
}
