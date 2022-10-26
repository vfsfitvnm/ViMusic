package it.vfsfitvnm.vimusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.components.themed.Switch
import it.vfsfitvnm.vimusic.ui.components.themed.ValueSelectorDialog
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SettingsScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabChanged) = rememberSaveable {
        mutableStateOf(0)
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Appearance", R.drawable.color_palette)
                    Item(1, "Player", R.drawable.play)
                    Item(2, "Cache", R.drawable.server)
                    Item(3, "Database", R.drawable.server)
                    Item(4, "Other", R.drawable.shapes)
                    Item(5, "About", R.drawable.information)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> AppearanceSettings()
                        1 -> PlayerSettings()
                        2 -> CacheSettings()
                        3 -> DatabaseSettings()
                        4 -> OtherSettings()
                        5 -> About()
                    }
                }
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
    isEnabled: Boolean = true,
    crossinline valueText: (T) -> String = Enum<T>::name,
    noinline trailingContent: (@Composable () -> Unit)? = null
) {
    ValueSelectorSettingsEntry(
        title = title,
        selectedValue = selectedValue,
        values = enumValues<T>().toList(),
        onValueSelected = onValueSelected,
        modifier = modifier,
        isEnabled = isEnabled,
        valueText = valueText,
        trailingContent = trailingContent,
    )
}

@Composable
inline fun <T> ValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    values: List<T>,
    crossinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    crossinline valueText: (T) -> String = { it.toString() },
    noinline trailingContent: (@Composable () -> Unit)? = null
) {
    var isShowingDialog by remember {
        mutableStateOf(false)
    }

    if (isShowingDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingDialog = false },
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
        isEnabled = isEnabled,
        onClick = { isShowingDialog = true },
        trailingContent = trailingContent
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
    SettingsEntry(
        title = title,
        text = text,
        isEnabled = isEnabled,
        onClick = { onCheckedChange(!isChecked) },
        trailingContent = { Switch(isChecked = isChecked) },
        modifier = modifier
    )
}

@Composable
fun SettingsEntry(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val (colorPalette, typography) = LocalAppearance.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(start = 16.dp)
            .padding(all = 16.dp)
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

        trailingContent?.invoke()
    }
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
            .padding(start = 16.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ImportantSettingsDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.semiBold.color(colorPalette.red),
        modifier = modifier
            .padding(start = 16.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
        style = typography.xxs.semiBold.copy(colorPalette.accent),
        modifier = modifier
            .padding(start = 16.dp)
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun SettingsGroupSpacer(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .height(24.dp)
    )
}
