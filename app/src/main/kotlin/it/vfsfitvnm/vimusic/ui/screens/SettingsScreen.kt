package it.vfsfitvnm.vimusic.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.Route0
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.route.fastFade
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.EnumValueSelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.settings.AppearanceScreen
import it.vfsfitvnm.vimusic.ui.screens.settings.BackupAndRestoreScreen
import it.vfsfitvnm.vimusic.ui.screens.settings.rememberAppearanceRoute
import it.vfsfitvnm.vimusic.ui.screens.settings.rememberBackupAndRestoreRoute
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

@ExperimentalAnimationApi
@Composable
fun SettingsScreen() {
    val albumRoute = rememberPlaylistOrAlbumRoute()
    val artistRoute = rememberArtistRoute()
    val appearanceRoute = rememberAppearanceRoute()
    val backupAndRestoreRoute = rememberBackupAndRestoreRoute()

    val scrollState = rememberScrollState()

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when (targetState.route) {
                appearanceRoute, backupAndRestoreRoute ->
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)
                else -> when (initialState.route) {
                    appearanceRoute, backupAndRestoreRoute ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
                    else -> fastFade
                }
            }
        }
    ) {
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

        appearanceRoute {
            AppearanceScreen()
        }

        backupAndRestoreRoute {
            BackupAndRestoreScreen()
        }

        host {
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current

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
                        text = "Settings",
                        style = typography.m.semiBold
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                @Composable
                fun Entry(
                    @DrawableRes icon: Int,
                    color: Color,
                    title: String,
                    description: String,
                    route: Route0
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(bounded = true),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    route()
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color = color, shape = CircleShape)
                                .size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.background),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(18.dp)
                            )
                        }

                        Column {
                            BasicText(
                                text = title,
                                style = typography.s.semiBold,
                                modifier = Modifier
                            )

                            BasicText(
                                text = description,
                                style = typography.xs.secondary.medium,
                                maxLines = 1,
                                modifier = Modifier
                            )
                        }
                    }
                }

                Entry(
                    color = colorPalette.blue,
                    icon = R.drawable.color_palette,
                    title = "Appearance",
                    description = "Change the colors and shapes of the app",
                    route = appearanceRoute,
                )

                Entry(
                    color = colorPalette.orange,
                    icon = R.drawable.server,
                    title = "Backup & Restore",
                    description = "Backup and restore the app database",
                    route = backupAndRestoreRoute
                )
            }
        }
    }
}

@Composable
inline fun <reified T: Enum<T>>EnumValueSelectorEntry(
    title: String,
    selectedValue: T,
    crossinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    crossinline valueText: (T) -> String = Enum<T>::name
) {
    val typography = LocalTypography.current

    var isShowingDialog by remember {
        mutableStateOf(false)
    }

    if (isShowingDialog) {
        EnumValueSelectorDialog(
            onDismiss = {
                isShowingDialog = false
            },
            title = title,
            selectedValue = selectedValue,
            onValueSelected = onValueSelected,
            valueText = valueText
        )
    }

    Column(
        modifier = modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = { isShowingDialog = true }
            )
            .padding(start = 24.dp)
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        BasicText(
            text = title,
            style = typography.xs.semiBold,
        )

        BasicText(
            text = valueText(selectedValue),
            style = typography.xs.semiBold.secondary
        )
    }
}