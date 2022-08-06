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
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.*
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.Switch
import it.vfsfitvnm.vimusic.ui.components.themed.ValueSelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.settings.*
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.*
import androidx.compose.ui.res.stringResource

@ExperimentalAnimationApi
@Composable
fun SettingsScreen() {
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
        globalRoutes()

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
            val (colorPalette, typography) = LocalAppearance.current

            var isFirstLaunch by rememberPreference(isFirstLaunchKey, true)

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
                    text = stringResource(R.string.settings),
                    style = typography.l.semiBold,
                    modifier = Modifier
                        .padding(start = 48.dp)
                        .padding(all = 16.dp)
                )

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
                                maxLines = 2
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
                    title = stringResource(R.string.appearance),
                    description = stringResource(R.string.appearance_desc),
                    route = appearanceSettingsRoute,
                )

                Entry(
                    color = colorPalette.blue,
                    icon = R.drawable.play,
                    title = stringResource(R.string.player_audio),
                    description = stringResource(R.string.player_desc),
                    route = playerSettingsRoute,
                )

                Entry(
                    color = colorPalette.cyan,
                    icon = R.drawable.server,
                    title = stringResource(R.string.cache),
                    description = stringResource(R.string.cache_desc),
                    route = cacheSettingsRoute
                )

                Entry(
                    color = colorPalette.orange,
                    icon = R.drawable.save,
                    title = stringResource(R.string.backup_restore),
                    description = stringResource(R.string.backup_desc),
                    route = backupAndRestoreRoute
                )

                Entry(
                    color = colorPalette.green,
                    icon = R.drawable.shapes,
                    title = stringResource(R.string.other),
                    description = stringResource(R.string.other_desc),
                    route = otherSettingsRoute,
                    withAlert = isFirstLaunch,
                    onClick = {
                        isFirstLaunch = false
                    }
                )

                Entry(
                    color = colorPalette.magenta,
                    icon = R.drawable.information,
                    title = stringResource(R.string.about),
                    description = stringResource(R.string.about_desc),
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
        onValueSelected = onValueSelected,
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
    val (colorPalette, typography) = LocalAppearance.current

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
    val (_, typography) = LocalAppearance.current
    val (colorPalette) = LocalAppearance.current

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
fun SettingsTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.m.semiBold,
        modifier = modifier
            .padding(start = 40.dp)
            .padding(all = 16.dp)
    )
}

@Composable
fun SettingsDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.secondary,
        modifier = modifier
            .padding(start = 56.dp, end = 24.dp)
            .padding(bottom = 16.dp)
    )
}

@Composable
fun SettingsGroupDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.secondary,
        modifier = modifier
            .padding(start = 56.dp, end = 24.dp)
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsEntryGroupText(
    title: String,
    modifier: Modifier = Modifier,
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = title.uppercase(),
        style = typography.xxs.semiBold.copy(colorPalette.blue),
        modifier = modifier
            .padding(start = 24.dp, top = 24.dp)
            .padding(horizontal = 32.dp)
    )
}
