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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.screens.SettingsDescription
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsGroupDescription
import it.vfsfitvnm.vimusic.ui.screens.SwitchSettingEntry
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.isIgnoringBatteryOptimizations
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold

@ExperimentalAnimationApi
@Composable
fun OtherSettingsScreen() {

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val context = LocalContext.current
            val (colorPalette, typography) = LocalAppearance.current

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

                BasicText(
                    text = "Other",
                    style = typography.m.semiBold,
                    modifier = Modifier
                        .padding(start = 40.dp)
                        .padding(all = 16.dp)
                )

                SettingsEntryGroupText(title = "SERVICE LIFETIME")

                SettingsGroupDescription(text = "Some device manufacturers may have an aggressive policy against stopped foreground services - the media notification can disappear suddenly when paused.\nThe gentle approach consists in disabling battery optimizations - this is enough for some devices and ROMs.\nHowever, if it's not, you can make the service \"invincible\" - which should keep the service alive.")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsDescription(text = "Since Android 12, the invincible service works ONLY if battery optimizations are disabled for this application.")
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
                    onCheckedChange = {
                        isInvincibilityEnabled = it
                    }
                )
            }
        }
    }
}
