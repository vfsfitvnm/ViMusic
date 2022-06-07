package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import it.vfsfitvnm.route.Route0

@Composable
fun rememberAppearanceRoute(): Route0 {
    return remember {
        Route0("AppearanceRoute")
    }
}
