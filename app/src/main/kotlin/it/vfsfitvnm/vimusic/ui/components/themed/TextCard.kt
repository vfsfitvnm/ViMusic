package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.align
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold

@Composable
fun TextCard(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconColor: ColorFilter? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable TextCardScope.() -> Unit,
) {
    val (colorPalette) = LocalAppearance.current

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                enabled = onClick != null,
                onClick = onClick ?: {}
            )
            .background(colorPalette.background1)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        icon?.let {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = iconColor ?: ColorFilter.tint(Color.Red),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(24.dp)
            )
        }

        (icon?.let { IconTextCardScopeImpl } ?: TextCardScopeImpl).content()
    }
}

interface TextCardScope {
    @Composable
    fun Title(text: String)

    @Composable
    fun Text(text: String)
}

private object TextCardScopeImpl : TextCardScope {
    @Composable
    override fun Title(text: String) {
        val (_, typography) = LocalAppearance.current
        BasicText(
            text = text,
            style = typography.xxs.semiBold,
        )
    }

    @Composable
    override fun Text(text: String) {
        val (_, typography) = LocalAppearance.current
        BasicText(
            text = text,
            style = typography.xxs.secondary.align(TextAlign.Justify),
        )
    }
}

private object IconTextCardScopeImpl : TextCardScope {
    @Composable
    override fun Title(text: String) {
        val (_, typography) = LocalAppearance.current
        BasicText(
            text = text,
            style = typography.xxs.semiBold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
    }

    @Composable
    override fun Text(text: String) {
        val (_, typography) = LocalAppearance.current
        BasicText(
            text = text,
            style = typography.xxs.secondary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
    }
}
