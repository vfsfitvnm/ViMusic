package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TabColumn(
    tabIndex: Int,
    onTabIndexChanged: (Int) -> Unit,
    selectedTextColor: Color,
    disabledTextColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    content: @Composable (@Composable (Int, String, Int) -> Unit) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        val transition = updateTransition(targetState = tabIndex, label = null)

        content { index, text, icon ->
            val dothAlpha by transition.animateFloat(label = "") {
                if (it == index) 1f else 0f
            }

            val textColor by transition.animateColor(label = "") {
                if (it == index) selectedTextColor else disabledTextColor
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onTabIndexChanged(index) }
                    )
                    .padding(horizontal = 8.dp)
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(selectedTextColor),
                    modifier = Modifier
                        .vertical()
                        .graphicsLayer {
                            alpha = dothAlpha
                            translationX = (1f - dothAlpha) * -48.dp.toPx()
                            rotationZ = -90f
                        }
                        .size(12.dp)
                )

                BasicText(
                    text = text,
                    style = textStyle.copy(color = textColor),
                    modifier = Modifier
                        .vertical()
                        .rotate(-90f)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

fun Modifier.vertical() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
