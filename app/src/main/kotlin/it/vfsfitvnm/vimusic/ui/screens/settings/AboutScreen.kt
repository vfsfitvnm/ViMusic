package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

@ExperimentalAnimationApi
@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette, typography) = LocalAppearance.current
            val uriHandler = LocalUriHandler.current

            Column(
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp)
            ) {
                TopAppBar(
                    modifier = Modifier
                        .height(52.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.chevron_back),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = pop)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )

                    BasicText(
                        text = "About",
                        style = typography.m.semiBold
                    )

                    Image(
                        painter = painterResource(R.drawable.logo_github),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic")
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(all = 32.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = colorPalette.primaryContainer, shape = CircleShape)
                            .padding(all = 16.dp)
                            .size(48.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.onPrimaryContainer),
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BasicText(
                            text = "ViMusic",
                            style = typography.l.bold,
                            modifier = Modifier
                                .alignByBaseline()
                        )
                        BasicText(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = typography.s.bold.secondary,
                            modifier = Modifier
                                .alignByBaseline()
                        )
                    }
                }
            }
        }
    }
}
