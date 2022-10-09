package it.vfsfitvnm.vimusic.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.Dimensions

@Composable
inline fun ItemContainer(
    alternative: Boolean,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable (centeredModifier: Modifier) -> Unit
) {
    if (alternative) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                .width(thumbnailSizeDp)
        ) {
            content(
                centeredModifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        }
    } else {
        Row(
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            content(
                centeredModifier = Modifier
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
inline fun ItemInfoContainer(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
        content = content
    )
}
