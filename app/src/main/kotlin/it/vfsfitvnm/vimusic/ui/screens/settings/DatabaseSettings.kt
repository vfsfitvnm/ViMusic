package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
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
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.checkpoint
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.path
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

@ExperimentalAnimationApi
@Composable
fun DatabaseSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    val queriesCount by produceSaveableState(initialValue = 0, stateSaver = autoSaver()) {
        Database.queriesCount()
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .collect { value = it }
    }

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.internal.checkpoint()

                context.applicationContext.contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(Database.internal.path).use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            Toast.makeText(
                context,
                "${context.applicationInfo.nonLocalizedLabel} is going to close itself after restoring the database",
                Toast.LENGTH_SHORT
            ).show()

            query {
                Database.internal.checkpoint()
                Database.internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(Database.internal.path).use(input::copyTo)
                }

                context.stopService(context.intent<PlayerService>())
                exitProcess(0)
            }
        }


    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LocalPlayerAwarePaddingValues.current)
    ) {
        Header(title = "Database")

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

        ImportantSettingsDescription(text = "Existing data will be overwritten.\n${context.applicationInfo.nonLocalizedLabel} will automatically close itself after restoring the database.")

        SettingsEntry(
            title = "Restore",
            text = "Import the database from the external storage",
            onClick = {
                restoreLauncher.launch(
                    arrayOf(
                        "application/x-sqlite3",
                        "application/vnd.sqlite3",
                        "application/octet-stream"
                    )
                )
            }
        )
    }
}
