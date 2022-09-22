package it.vfsfitvnm.vimusic.ui.components.themed

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.components.TabColumn
import it.vfsfitvnm.vimusic.ui.components.vertical
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.semiBold

@SuppressLint("ModifierParameter")
@Composable
fun VerticalBar(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    tabColumnContent: @Composable (@Composable (Int, String, Int) -> Unit) -> Unit,
//    primaryIconButtonId: Int? = null,
//    onPrimaryIconButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
    ) {
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(16.dp))
//                .clickable(onClick = onTopIconButtonClick)
//                .background(color = colorPalette.background1)
//                .size(48.dp)
//        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onTopIconButtonClick)
                    .padding(all = 12.dp)
//                    .align(Alignment.Center)
                    .size(22.dp)
            )
//        }

        Spacer(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
        )

        TabColumn(
            tabIndex = tabIndex,
            onTabIndexChanged = onTabChanged,
            selectedTextColor = colorPalette.text,
            disabledTextColor = colorPalette.textDisabled,
            textStyle = typography.xs.semiBold,
            content = tabColumnContent,
        )

//        Spacer(
//            modifier = Modifier
//                .weight(1f)
//        )

//        primaryIconButtonId?.let {
//            Box(
//                modifier = Modifier
//                    .offset(x = 8.dp)
//                    .clip(RoundedCornerShape(16.dp))
//                    .clickable(onClick = onPrimaryIconButtonClick)
//                    .background(colorPalette.background1)
//                    .size(62.dp)
//            ) {
//                Image(
//                    painter = painterResource(primaryIconButtonId),
//                    contentDescription = null,
//                    colorFilter = ColorFilter.tint(colorPalette.text),
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .size(20.dp)
//                )
//            }
//        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun VerticalTitleBar(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    title: String,
    primaryIconButtonId: Int? = null,
    primaryIconButtonEnabled: Boolean = true,
    onPrimaryIconButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onTopIconButtonClick)
                .background(color = colorPalette.background1)
                .size(48.dp)
        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .width(78.dp)
                .height(32.dp)
        )

        BasicText(
            text = title,
            style = typography.m.semiBold,
            modifier = Modifier
                .vertical()
                .rotate(-90f)
                .padding(horizontal = 16.dp)
        )

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        primaryIconButtonId?.let {
            Box(
                modifier = Modifier
                    .offset(x = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = primaryIconButtonEnabled, onClick = onPrimaryIconButtonClick)
                    .background(colorPalette.background1)
                    .size(62.dp)
            ) {
                Image(
                    painter = painterResource(primaryIconButtonId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            }
        }
    }
}
