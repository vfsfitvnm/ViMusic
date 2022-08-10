package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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

    val backgroundColor by animateColorAsState(if (isChecked) colorPalette.accent else colorPalette.background1)
    val color by animateColorAsState(if (isChecked) colorPalette.onAccent else colorPalette.textDisabled)
    val offset by animateDpAsState(if (isChecked) 36.dp else 12.dp)

    Spacer(
        modifier = modifier
            .width(48.dp)
            .height(24.dp)
            .background(color = backgroundColor, shape = CircleShape)
            .drawBehind {
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
    )
}
