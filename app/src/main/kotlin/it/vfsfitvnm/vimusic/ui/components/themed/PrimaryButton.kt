package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@Composable
fun BoxScope.PrimaryButton(
    onClick: () -> Unit,
    @DrawableRes iconId: Int,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val (colorPalette) = LocalAppearance.current

    Box(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(all = 16.dp)
            .padding(LocalPlayerAwarePaddingValues.current)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .background(colorPalette.background2)
            .size(62.dp)
    ) {
        Image(
            painter = painterResource(iconId),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text),
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp)
        )
    }
}
