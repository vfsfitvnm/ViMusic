package it.vfsfitvnm.vimusic.ui.screens.settings

import android.text.format.Formatter
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.CoilDiskCacheMaxSize
import it.vfsfitvnm.vimusic.enums.ExoPlayerDiskCacheMaxSize
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.EnumValueSelectorSettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsDescription
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsGroupDescription
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.coilDiskCacheMaxSizeKey
import it.vfsfitvnm.vimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@Composable
fun CacheSettingsScreen() {

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val context = LocalContext.current
            val (colorPalette, _) = LocalAppearance.current
            val binder = LocalPlayerServiceBinder.current

            var coilDiskCacheMaxSize by rememberPreference(
                coilDiskCacheMaxSizeKey,
                CoilDiskCacheMaxSize.`128MB`
            )
            var exoPlayerDiskCacheMaxSize by rememberPreference(
                exoPlayerDiskCacheMaxSizeKey,
                ExoPlayerDiskCacheMaxSize.`2GB`
            )

            val coroutineScope = rememberCoroutineScope()

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
                }

                SettingsTitle(text = "Cache")

                SettingsDescription(text = "When the cache runs out of space, the resources that haven't been accessed for the longest time are cleared.")

                Coil.imageLoader(context).diskCache?.let { diskCache ->
                    var diskCacheSize by remember(diskCache) {
                        mutableStateOf(diskCache.size)
                    }

                    SettingsEntryGroupText(title = "IMAGE CACHE")

                    SettingsGroupDescription(text = "${Formatter.formatShortFileSize(context, diskCacheSize)} used (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)")

                    EnumValueSelectorSettingsEntry(
                        title = "Max size",
                        selectedValue = coilDiskCacheMaxSize,
                        onValueSelected = {
                            coilDiskCacheMaxSize = it
                        }
                    )

                    SettingsEntry(
                        title = "Clear space",
                        text = "Wipe every cached image",
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                diskCache.clear()
                                diskCacheSize = diskCache.size
                            }
                        }
                    )
                }

                binder?.cache?.let { cache ->
                    val diskCacheSize by remember {
                        derivedStateOf {
                            cache.cacheSpace
                        }
                    }

                    SettingsEntryGroupText(title = "SONG CACHE")

                    SettingsGroupDescription(
                        text = buildString {
                            append(Formatter.formatShortFileSize(context, diskCacheSize))
                            append(" used")
                            when (val size = exoPlayerDiskCacheMaxSize) {
                                ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                                else -> append(" (${diskCacheSize * 100 / size.bytes}%)")
                            }
                        }
                    )

                    EnumValueSelectorSettingsEntry(
                        title = "Max size",
                        selectedValue = exoPlayerDiskCacheMaxSize,
                        onValueSelected = {
                            exoPlayerDiskCacheMaxSize = it
                        }
                    )
                }
            }
        }
    }
}
