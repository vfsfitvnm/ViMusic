package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import it.vfsfitvnm.route.Route0

@Composable
fun rememberAppearanceSettingsRoute(): Route0 {
    return remember {
        Route0("AppearanceSettingsRoute")
    }
}

@Composable
fun rememberPlayerSettingsRoute(): Route0 {
    return remember {
        Route0("PlayerSettingsRoute")
    }
}

@Composable
fun rememberBackupAndRestoreRoute(): Route0 {
    return remember {
        Route0("BackupAndRestoreRoute")
    }
}

@Composable
fun rememberCacheSettingsRoute(): Route0 {
    return remember {
        Route0("CacheSettingsRoute")
    }
}

@Composable
fun rememberAboutRoute(): Route0 {
    return remember {
        Route0("AboutRoute")
    }
}
