package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import kotlin.random.Random

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = LocalAppearance.current.colorPalette.shimmer
) {
    Spacer(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(color)
            .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
            .height(16.dp)
    )
}
