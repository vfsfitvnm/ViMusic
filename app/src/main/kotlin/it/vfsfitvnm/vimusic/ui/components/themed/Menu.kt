package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.semiBold

@Composable
inline fun Menu(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorPalette = LocalColorPalette.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .width(256.dp)
            .background(
                color = colorPalette.elevatedBackground,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(vertical = 8.dp),
        content = content
    )
}

@Composable
inline fun BasicMenu(
    noinline onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Menu(modifier = modifier) {
        MenuCloseButton(onClick = onDismiss)
        content()
    }
}


@Composable
fun MenuEntry(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    secondaryText: String? = null,
    enabled: Boolean = true,
) {
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick
            )
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (enabled) colorPalette.textSecondary else colorPalette.textDisabled),
            modifier = Modifier
                .size(18.dp)
        )

        Column {
            BasicText(
                text = text,
                style = typography.xs.semiBold.color(if (enabled) colorPalette.text else colorPalette.textDisabled)
            )

            secondaryText?.let { secondaryText ->
                BasicText(
                    text = secondaryText,
                    style = typography.xxs.semiBold.color(if (enabled) colorPalette.textSecondary else colorPalette.textDisabled)
                )
            }
        }
    }
}

@Composable
fun MenuIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPalette = LocalColorPalette.current

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text),
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .size(20.dp)
        )
    }
}

@Composable
fun MenuCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MenuIconButton(
        icon = R.drawable.close,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun MenuBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MenuIconButton(
        icon = R.drawable.chevron_back,
        onClick = onClick,
        modifier = modifier
    )
}


