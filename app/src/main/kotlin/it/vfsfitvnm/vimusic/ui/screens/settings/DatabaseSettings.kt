package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import it.vfsfitvnm.vimusic.*
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
import kotlinx.coroutines.flow.distinctUntilChanged

@ExperimentalAnimationApi
@Composable
fun DatabaseSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    val eventsCount by remember {
        Database.eventsCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()

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
                Database.checkpoint()
                Database.internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        FileOutputStream(Database.internal.path).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
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
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = stringResource(id = R.string.database))

        SettingsEntryGroupText(title = stringResource(id = R.string.cleanup).uppercase())

        SettingsEntry(
            title = stringResource(id = R.string.reset_quick_picks),
            text = if (eventsCount > 0) {
                stringResource(id = R.string.delete) + " $eventsCount " + stringResource(id = R.string.playback_events)
            } else {
                stringResource(id = R.string.quick_picks_are_cleared)
            },
            isEnabled = eventsCount > 0,
            onClick = { query(Database::clearEvents) }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(id = R.string.backup).uppercase())

        SettingsDescription(text = stringResource(id = R.string.backup_description))

        SettingsEntry(
            title = stringResource(id = R.string.backup),
            text = stringResource(id = R.string.backup_action_description),
            onClick = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")

                try {
                    backupLauncher.launch("vimusic_${dateFormat.format(Date())}.db")
                } catch (e: ActivityNotFoundException) {
                    context.toast(context.getString(R.string.could_not_find_app_to_create_documents))
                }
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(id = R.string.restore))

        ImportantSettingsDescription(
            text = stringResource(id = R.string.existing_data_will_be_overwritten) + "\n${context.applicationInfo.nonLocalizedLabel} " + stringResource(
                id = R.string.will_automatically_close_after_restoring
            )
        )

        SettingsEntry(
            title = stringResource(id = R.string.restore),
            text = stringResource(id = R.string.restore_description),
            onClick = {
                try {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/vnd.sqlite3",
                            "application/x-sqlite3",
                            "application/octet-stream"
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    context.toast(context.getString(R.string.could_not_find_app_to_open_documents))
                }
            }
        )
    }
}
