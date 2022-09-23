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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.checkpoint
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.path
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.isIgnoringBatteryOptimizations
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers

@ExperimentalAnimationApi
@Composable
fun OtherSettingsTab() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    val queriesCount by remember {
        Database.queriesCount()
    }.collectAsState(initial = 0, context = Dispatchers.IO)

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    var isShowingRestoreDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.internal.checkpoint()
                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        FileInputStream(Database.internal.path).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.internal.checkpoint()
                Database.internal.close()

                FileOutputStream(Database.internal.path).use { outputStream ->
                    context.applicationContext.contentResolver.openInputStream(uri)
                        ?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                }

                context.stopService(context.intent<PlayerService>())
                exitProcess(0)
            }
        }

    if (isShowingRestoreDialog) {
        ConfirmationDialog(
            text = "The application will automatically close itself to avoid problems after restoring the database.",
            onDismiss = {
                isShowingRestoreDialog = false
            },
            onConfirm = {
                restoreLauncher.launch(
                    arrayOf(
                        "application/x-sqlite3",
                        "application/vnd.sqlite3",
                        "application/octet-stream"
                    )
                )
            },
            confirmText = "Ok"
        )
    }

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LocalPlayerAwarePaddingValues.current)
    ) {
        Header(title = "Other")

        SettingsEntryGroupText(title = "SEARCH HISTORY")

        SettingsEntry(
            title = "Clear search history",
            text = if (queriesCount > 0) {
                "Delete $queriesCount search queries"
            } else {
                "History is empty"
            },
            isEnabled = queriesCount > 0,
            onClick = {
                query {
                    Database.clearQueries()
                }
            }
        )

        SettingsGroupSpacer()

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

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "BACKUP")

        SettingsDescription(text = "Personal preferences (i.e. the theme mode) and the cache are excluded.")

        SettingsEntry(
            title = "Backup",
            text = "Export the database to the external storage",
            onClick = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                backupLauncher.launch("vimusic_${dateFormat.format(Date())}.db")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "RESTORE")

        SettingsDescription(text = "Existing data will be overwritten.")

        SettingsEntry(
            title = "Restore",
            text = "Import the database from the external storage",
            onClick = {
                isShowingRestoreDialog = true
            }
        )
    }
}
