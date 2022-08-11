package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.checkpoint
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.path
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntry
import it.vfsfitvnm.vimusic.ui.screens.SettingsEntryGroupText
import it.vfsfitvnm.vimusic.ui.screens.SettingsGroupDescription
import it.vfsfitvnm.vimusic.ui.screens.SettingsTitle
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.intent
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

@ExperimentalAnimationApi
@Composable
fun BackupAndRestoreScreen() {
    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val (colorPalette) = LocalAppearance.current
            val context = LocalContext.current

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

            var isShowingRestoreDialog by rememberSaveable {
                mutableStateOf(false)
            }

            if (isShowingRestoreDialog) {
                ConfirmationDialog(
                    text = stringResource(R.string.restore_dialog),
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
                    .verticalScroll(scrollState)
                    .padding(bottom = Dimensions.collapsedPlayer)
                    .systemBarsPadding()
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

                SettingsTitle(text = stringResource(R.string.backup_restore))

                SettingsEntryGroupText(title = stringResource(R.string.backup))

                SettingsGroupDescription(text = stringResource(R.string.backup_sub_desc))

                SettingsEntry(
                    title = stringResource(R.string.backup),
                    text = stringResource(R.string.backup_export),
                    onClick = {
                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        backupLauncher.launch("vimusic_${dateFormat.format(Date())}.db")
                    }
                )

                SettingsEntryGroupText(title = stringResource(R.string.restore))

                SettingsGroupDescription(text = stringResource(R.string.restore_warning))

                SettingsEntry(
                    title = stringResource(R.string.restore),
                    text = stringResource(R.string.restore_import),
                    onClick = {
                        isShowingRestoreDialog = true
                    }
                )
            }
        }
    }
}
