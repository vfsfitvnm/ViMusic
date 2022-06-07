package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
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
import it.vfsfitvnm.vimusic.ui.screens.ArtistScreen
import it.vfsfitvnm.vimusic.ui.screens.PlaylistOrAlbumScreen
import it.vfsfitvnm.vimusic.ui.screens.rememberArtistRoute
import it.vfsfitvnm.vimusic.ui.screens.rememberPlaylistOrAlbumRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

@ExperimentalAnimationApi
@Composable
fun AboutScreen() {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
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