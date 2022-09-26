package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.components.TabColumn
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.semiBold

@Composable
fun VerticalBar(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    tabColumnContent: @Composable (@Composable (Int, String, Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(topIconButtonId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.textSecondary),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onTopIconButtonClick)
                .padding(all = 12.dp)
                .size(22.dp)
        )

        Spacer(
            modifier = Modifier
                .width(Dimensions.verticalBarWidth)
                .height(32.dp)
        )

        TabColumn(
            tabIndex = tabIndex,
            onTabIndexChanged = onTabChanged,
            selectedTextColor = colorPalette.text,
            disabledTextColor = colorPalette.textDisabled,
            textStyle = typography.xs.semiBold,
            content = tabColumnContent,
        )
    }
}
