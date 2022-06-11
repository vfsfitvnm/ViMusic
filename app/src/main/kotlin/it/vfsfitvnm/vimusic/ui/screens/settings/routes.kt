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

@Composable
fun rememberNotificationRoute(): Route0 {
    return remember {
        Route0("NotificationRoute")
    }
}

@Composable
fun rememberBackupAndRestoreRoute(): Route0 {
    return remember {
        Route0("BackupAndRestoreRoute")
    }
}

@Composable
fun rememberOtherRoute(): Route0 {
    return remember {
        Route0("OtherRoute")
    }
}

@Composable
fun rememberAboutRoute(): Route0 {
    return remember {
        Route0("AboutRoute")
    }
}
