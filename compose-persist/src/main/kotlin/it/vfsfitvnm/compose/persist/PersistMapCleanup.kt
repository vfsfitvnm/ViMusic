package it.vfsfitvnm.compose.persist

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun PersistMapCleanup(tagPrefix: String) {
    val context = LocalContext.current

    DisposableEffect(context) {
        onDispose {
            if (context.findOwner<Activity>()?.isChangingConfigurations == false) {
                context.persistMap?.keys?.removeAll { it.startsWith(tagPrefix) }
            }
        }
    }
}
