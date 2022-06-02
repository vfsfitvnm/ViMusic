package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MusicBars(
    color: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    shape: Shape = CircleShape
) {
    val animatablesWithSteps = remember {
        listOf(
            Animatable(0f) to listOf(0.2f, 0.8f, 0.1f, 0.1f, 0.3f, 0.1f, 0.2f, 0.8f, 0.7f, 0.2f, 0.4f, 0.9f, 0.7f, 0.6f, 0.1f, 0.3f, 0.1f, 0.4f, 0.1f, 0.8f, 0.7f, 0.9f, 0.5f, 0.6f, 0.3f, 0.1f),
            Animatable(0f) to listOf(0.2f, 0.5f, 1.0f, 0.5f, 0.3f, 0.1f, 0.2f, 0.3f, 0.5f, 0.1f, 0.6f, 0.5f, 0.3f, 0.7f, 0.8f, 0.9f, 0.3f, 0.1f, 0.5f, 0.3f, 0.6f, 1.0f, 0.6f, 0.7f, 0.4f, 0.1f),
            Animatable(0f) to listOf(0.6f, 0.5f, 1.0f, 0.6f, 0.5f, 1.0f, 0.6f, 0.5f, 1.0f, 0.5f, 0.6f, 0.7f, 0.2f, 0.3f, 0.1f, 0.5f, 0.4f, 0.6f, 0.7f, 0.1f, 0.4f, 0.3f, 0.1f, 0.4f, 0.3f, 0.7f)
        )
    }

    LaunchedEffect(Unit) {
        animatablesWithSteps.forEach { (animatable, steps) ->
            launch {
                while (true) {
                    steps.forEach { step ->
                        animatable.animateTo(step)
                    }
                }
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        animatablesWithSteps.forEach { (animatable) ->
            Spacer(
                modifier = Modifier
                    .background(color = color, shape = shape)
                    .fillMaxHeight(animatable.value)
                    .width(barWidth)
            )
        }
    }
}