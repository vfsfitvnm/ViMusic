package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    text: String,
    style: TextStyle,
    showMoreTextStyle: TextStyle,
    minimizedMaxLines: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }

    var hasVisualOverflow by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = modifier
    ) {
        Box {
            BasicText(
                text = text,
                maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
                onTextLayout = {
                    hasVisualOverflow = it.hasVisualOverflow
                },
                style = style
            )

            if (hasVisualOverflow) {
                Spacer(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = 0.5f),
                                    backgroundColor
                                )
                            )
                        )
                )
            }
        }

        BasicText(
            text = if (isExpanded) "Less" else "More",
            style = showMoreTextStyle,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { isExpanded = !isExpanded }
                )
        )
    }
}
