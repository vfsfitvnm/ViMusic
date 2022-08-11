package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@ExperimentalAnimationApi
@Composable
fun PlayerBottomSheet(
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val (colorPalette) = LocalAppearance.current

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(backgroundColorProvider()) }
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                Image(
                    painter = painterResource(R.drawable.playlist),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp)
                )

                content()
            }
        }
    ) {
        CurrentPlaylistView(
            layoutState = layoutState,
            onGlobalRouteEmitted = onGlobalRouteEmitted,
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        )
    }
}
