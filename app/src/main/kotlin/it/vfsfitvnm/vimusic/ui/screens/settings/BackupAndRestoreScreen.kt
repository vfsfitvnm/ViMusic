package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.checkpoint
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.screens.ArtistScreen
import it.vfsfitvnm.vimusic.ui.screens.PlaylistOrAlbumScreen
import it.vfsfitvnm.vimusic.ui.screens.rememberArtistRoute
import it.vfsfitvnm.vimusic.ui.screens.rememberPlaylistOrAlbumRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


@ExperimentalAnimationApi
@Composable
fun BackupAndRestoreScreen() {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            PlaylistOrAlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val context = LocalContext.current

            val coroutineScope = rememberCoroutineScope()

            val backupLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
                    if (uri == null) return@rememberLauncherForActivityResult

                    coroutineScope.launch(Dispatchers.IO) {
                        Database.checkpoint()
                        context.applicationContext.contentResolver.openOutputStream(uri)
                            ?.use { outputStream ->
                                FileInputStream(Database.internal.getOpenHelper().writableDatabase.path).use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                    }
                }

            val restoreLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri == null) return@rememberLauncherForActivityResult

                    coroutineScope.launch(Dispatchers.IO) {
                        Database.internal.close()

                        FileOutputStream(Database.internal.getOpenHelper().writableDatabase.path).use { outputStream ->
                            context.applicationContext.contentResolver.openInputStream(uri)
                                ?.use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                        }

                        exitProcess(0)
                    }
                }

            var isShowingRestoreDialog by rememberSaveable {
                mutableStateOf(false)
            }

            if (isShowingRestoreDialog) {
                ConfirmationDialog(
                    text = "The application will automatically close itself to avoid problems after restoring the database.",
                    onDismiss = {
                        isShowingRestoreDialog = false
                    },
                    onConfirm = {
                        restoreLauncher.launch(
                            arrayOf("application/x-sqlite3", "application/vnd.sqlite3", "application/octet-stream")
                        )
                    },
                    confirmText = "Ok"
                )
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

                    BasicText(
                        text = "Backup & Restore",
                        style = typography.m.semiBold
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clickable {
                                    @SuppressLint("SimpleDateFormat")
                                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                                    backupLauncher.launch("vimusic_${dateFormat.format(Date())}.db")
                                }
                                .shadow(elevation = 8.dp, shape = CircleShape)
                                .background(
                                    color = colorPalette.elevatedBackground,
                                    shape = CircleShape
                                )
                                .size(92.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.share),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.blue),
                                modifier = Modifier
                                    .size(32.dp)
                            )
                        }

                        BasicText(
                            text = "Backup",
                            style = typography.xs.semiBold,
                            modifier = Modifier
                        )
                    }


                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clickable {
                                    isShowingRestoreDialog = true
                                }
                                .shadow(elevation = 8.dp, shape = CircleShape)
                                .background(
                                    color = colorPalette.elevatedBackground,
                                    shape = CircleShape
                                )
                                .size(92.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.download),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.orange),
                                modifier = Modifier
                                    .size(32.dp)
                            )
                        }

                        BasicText(
                            text = "Restore",
                            style = typography.xs.semiBold,
                            modifier = Modifier
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(colorPalette.lightBackground)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.alert_circle),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.red),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .size(24.dp)
                    )
//
                    BasicText(
                        text = "Backup",
                        style = typography.xxs.semiBold,
                        modifier = Modifier
                                .padding(horizontal = 16.dp)
                    )

                    BasicText(
                        text = "The backup consists in exporting the application database to your device storage.\nThis means playlists, song history, favorites songs will exported.\nThis operation excludes personal preferences such as the theme mode and everything you can set in the Settings page.",
                        style = typography.xxs.secondary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                    )

                    BasicText(
                        text = "Restore",
                        style = typography.xxs.semiBold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )

                    BasicText(
                        text = "The restore replaces the existing application database with the selected - previously exported - one.\nThis means every currently existing data will be wiped: THE TWO DATABASES WON'T BE MERGED.\nIt is recommended to restore the database immediately after the application is installed on a new device.",
                        style = typography.xxs.secondary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}