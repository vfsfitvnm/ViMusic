package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun <T>ChipGroup(
    items: List<ChipItem<T>>,
    value: T,
    selectedBackgroundColor: Color,
    unselectedBackgroundColor: Color,
    selectedTextStyle: TextStyle,
    unselectedTextStyle: TextStyle,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    onValueChanged: (T) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .then(modifier)
    ) {
        items.forEach { chipItem ->
            ChunkyButton(
                text = chipItem.text,
                textStyle = if (chipItem.value == value) selectedTextStyle else unselectedTextStyle,
                backgroundColor = if (chipItem.value == value) selectedBackgroundColor else unselectedBackgroundColor,
                shape = shape,
                onClick = {
                    onValueChanged(chipItem.value)
                }
            )
        }
    }
}

data class ChipItem<T>(
    val text: String,
    val value: T
)