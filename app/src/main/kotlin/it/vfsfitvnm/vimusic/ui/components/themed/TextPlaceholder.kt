package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import kotlin.random.Random

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier
) {
    val (colorPalette) = LocalAppearance.current
    Spacer(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(color = colorPalette.darkGray, shape = RoundedCornerShape(0.dp))
            .fillMaxWidth(remember { 0.25f + Random.nextFloat() * 0.5f })
            .height(16.dp)
    )
}
