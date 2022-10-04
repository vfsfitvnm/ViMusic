package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.isIgnoringBatteryOptimizations
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference

@ExperimentalAnimationApi
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LocalPlayerAwarePaddingValues.current)
    ) {
        Header(title = "Other")

        SettingsEntryGroupText(title = "SERVICE LIFETIME")

        SettingsDescription(text = "If battery optimizations are applied, the playback notification can suddenly disappear when paused.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsDescription(text = "Since Android 12, disabling battery optimizations is required for the \"Invincible service\" option to take effect.")
        }

        SettingsEntry(
            title = "Ignore battery optimizations",
            isEnabled = !isIgnoringBatteryOptimizations,
            text = if (isIgnoringBatteryOptimizations) {
                "Already unrestricted"
            } else {
                "Disable background restrictions"
            },
            onClick = {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@SettingsEntry

                @SuppressLint("BatteryLife")
                val intent =
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }

                if (intent.resolveActivity(context.packageManager) != null) {
                    activityResultLauncher.launch(intent)
                } else {
                    val fallbackIntent =
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

                    if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                        activityResultLauncher.launch(fallbackIntent)
                    } else {
                        Toast.makeText(
                            context,
                            "Couldn't find battery optimization settings, please whitelist ViMusic manually",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        SwitchSettingEntry(
            title = "Invincible service",
            text = "When turning off battery optimizations is not enough",
            isChecked = isInvincibilityEnabled,
            onCheckedChange = { isInvincibilityEnabled = it }
        )
    }
}
