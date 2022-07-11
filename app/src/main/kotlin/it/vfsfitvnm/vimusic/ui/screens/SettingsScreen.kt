package it.vfsfitvnm.vimusic.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.*
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.Switch
import it.vfsfitvnm.vimusic.ui.components.themed.ValueSelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.settings.*
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.*


@ExperimentalAnimationApi
@Composable
fun SettingsScreen() {
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()
    val appearanceSettingsRoute = rememberAppearanceSettingsRoute()
    val playerSettingsRoute = rememberPlayerSettingsRoute()
    val backupAndRestoreRoute = rememberBackupAndRestoreRoute()
    val cacheSettingsRoute = rememberCacheSettingsRoute()
    val otherSettingsRoute = rememberOtherSettingsRoute()
    val aboutRoute = rememberAboutRoute()

    val scrollState = rememberScrollState()

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when (targetState.route) {
                albumRoute, artistRoute -> fastFade
                else -> when (initialState.route) {
                    albumRoute, artistRoute -> fastFade
                    null -> leftSlide
                    else -> rightSlide
                }
            }
        }
    ) {
        albumRoute { browseId ->
            AlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        appearanceSettingsRoute {
            AppearanceSettingsScreen()
        }

        playerSettingsRoute {
            PlayerSettingsScreen()
        }

        backupAndRestoreRoute {
            BackupAndRestoreScreen()
        }

        cacheSettingsRoute {
            CacheSettingsScreen()
        }

        otherSettingsRoute {
            OtherSettingsScreen()
        }

        aboutRoute {
            AboutScreen()
        }

        host {
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val preferences = LocalPreferences.current

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
                    route: Route0,
                    withAlert: Boolean = false,
                    onClick: (() -> Unit)? = null
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
                                    onClick?.invoke()
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

                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            BasicText(
                                text = title,
                                style = typography.s.semiBold,
                            )

                            BasicText(
                                text = description,
                                style = typography.xs.secondary.medium,
                                maxLines = 1
                            )
                        }

                        if (withAlert) {
                            Canvas(
                                modifier = Modifier
                                    .size(8.dp)
                            ) {
                                drawCircle(
                                    color = colorPalette.red,
                                    center = size.center.copy(x = size.width),
                                    radius = 4.dp.toPx(),
                                    shadow = Shadow(
                                        color = colorPalette.red,
                                        blurRadius = 4.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                }

                Entry(
                    color = colorPalette.magenta,
                    icon = R.drawable.color_palette,
                    title = "Appearance",
                    description = "Change the colors and shapes of the app",
                    route = appearanceSettingsRoute,
                )

                Entry(
                    color = colorPalette.blue,
                    icon = R.drawable.play,
                    title = "Player & Audio",
                    description = "Player and audio settings",
                    route = playerSettingsRoute,
                )

                Entry(
                    color = colorPalette.cyan,
                    icon = R.drawable.server,
                    title = "Cache",
                    description = "Manage the used space",
                    route = cacheSettingsRoute
                )

                Entry(
                    color = colorPalette.orange,
                    icon = R.drawable.save,
                    title = "Backup & Restore",
                    description = "Backup and restore the app database",
                    route = backupAndRestoreRoute
                )

                Entry(
                    color = colorPalette.green,
                    icon = R.drawable.shapes,
                    title = "Other",
                    description = "Advanced settings",
                    route = otherSettingsRoute,
                    withAlert = LocalPreferences.current.isFirstLaunch,
                    onClick = {
                        preferences.isFirstLaunch = false
                    }
                )

                Entry(
                    color = colorPalette.magenta,
                    icon = R.drawable.information,
                    title = "About",
                    description = "App version and social links",
                    route = aboutRoute
                )
            }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> EnumValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    crossinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    crossinline valueText: (T) -> String = Enum<T>::name
) {
    ValueSelectorSettingsEntry(
        title = title,
        selectedValue = selectedValue,
        values = enumValues<T>().toList(),
        onValueSelected =onValueSelected,
        modifier = modifier,
        valueText = valueText
    )
}

@Composable
inline fun <T> ValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    values: List<T>,
    crossinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    crossinline valueText: (T) -> String = { it.toString() }
) {
    var isShowingDialog by remember {
        mutableStateOf(false)
    }

    if (isShowingDialog) {
        ValueSelectorDialog(
            onDismiss = {
                isShowingDialog = false
            },
            title = title,
            selectedValue = selectedValue,
            values = values,
            onValueSelected = onValueSelected,
            valueText = valueText
        )
    }

    SettingsEntry(
        title = title,
        text = valueText(selectedValue),
        modifier = modifier,
        onClick = {
            isShowingDialog = true
        }
    )
}


@Composable
fun SwitchSettingEntry(
    title: String,
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onCheckedChange(!isChecked) },
                enabled = isEnabled
            )
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(start = 24.dp)
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            BasicText(
                text = title,
                style = typography.xs.semiBold.copy(color = colorPalette.text),
            )

            BasicText(
                text = text,
                style = typography.xs.semiBold.copy(color = colorPalette.textSecondary),
            )
        }

        Switch(isChecked = isChecked)
    }
}

@Composable
fun SettingsEntry(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    val typography = LocalTypography.current
    val colorPalette = LocalColorPalette.current

    Column(
        modifier = modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                enabled = isEnabled
            )
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(start = 24.dp)
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        BasicText(
            text = title,
            style = typography.xs.semiBold.copy(color = colorPalette.text),
        )

        BasicText(
            text = text,
            style = typography.xs.semiBold.copy(color = colorPalette.textSecondary),
        )
    }
}

@Composable
@NonRestartableComposable
fun DisabledSettingsEntry(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    BaseSettingsEntry(
        title = title,
        text = text,
        modifier = modifier,
        titleTextStyle = { disabled },
        textStyle = { disabled },
    )
}

@Composable
fun BaseSettingsEntry(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    titleTextStyle: @Composable TextStyle.() -> TextStyle = { this },
    textStyle: @Composable TextStyle.() -> TextStyle = { this },
) {
    val typography = LocalTypography.current

    Column(
        modifier = modifier
            .padding(start = 24.dp)
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        BasicText(
            text = title,
            style = typography.xs.semiBold.run { titleTextStyle() },
        )

        BasicText(
            text = text,
            style = typography.xs.semiBold.secondary.run { textStyle() }
        )
    }
}

@Composable
fun SettingsEntryGroupText(
    title: String,
    modifier: Modifier = Modifier,
) {
    val typography = LocalTypography.current

    BasicText(
        text = title.uppercase(),
        style = typography.xxs.semiBold.copy(LocalColorPalette.current.blue),
        modifier = modifier
            .padding(start = 24.dp, top = 24.dp)
            .padding(horizontal = 32.dp)
    )
}
