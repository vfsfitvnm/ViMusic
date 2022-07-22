package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.BottomSheet
import it.vfsfitvnm.vimusic.ui.components.BottomSheetState
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.PlayerState


@ExperimentalAnimationApi
@Composable
fun PlayerBottomSheet(
    layoutState: BottomSheetState,
    onShowLyrics: () -> Unit,
    onShowStatsForNerds: () -> Unit,
    onGlobalRouteEmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (colorPalette) = LocalAppearance.current

    BottomSheet(
        state = layoutState,
        elevation = 16.dp,
        modifier = modifier,
        collapsedContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - (layoutState.progress * 16).coerceAtMost(1f)
                    }
                    .fillMaxWidth()
                    .height(layoutState.lowerBound)
                    .background(colorPalette.background)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )
                }

                Image(
                    painter = painterResource(R.drawable.chevron_up),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .size(18.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.text),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = onShowLyrics)
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.information),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = onShowStatsForNerds)
                            .padding(all = 8.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    ) {
        CurrentPlaylistView(
            layoutState = layoutState,
            onGlobalRouteEmitted = onGlobalRouteEmitted,
            modifier = Modifier
                .background(colorPalette.background)
                .fillMaxSize()
        )
    }
}
