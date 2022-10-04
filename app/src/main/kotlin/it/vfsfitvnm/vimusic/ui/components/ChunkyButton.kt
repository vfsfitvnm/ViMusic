package it.vfsfitvnm.vimusic.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.R

@Composable
fun ChunkyButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    text: String? = null,
    secondaryText: String? = null,
    textStyle: TextStyle = TextStyle.Default,
    secondaryTextStyle: TextStyle = TextStyle.Default,
    rippleColor: Color = Color.Unspecified,
    @DrawableRes icon: Int? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    colorFilter: ColorFilter = ColorFilter.tint(rippleColor),
    isEnabled: Boolean = true,
    onMore: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        icon?.let { icon ->
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = Modifier
                    .size(20.dp)
            )
        }

        text?.let { text ->
            Column {
                BasicText(
                    text = text,
                    style = textStyle
                )

                secondaryText?.let { secondaryText ->
                    BasicText(
                        text = secondaryText,
                        style = secondaryTextStyle
                    )
                }
            }
        }

        onMore?.let { onMore ->
            Spacer(
                modifier = Modifier
                    .background(rippleColor.copy(alpha = 0.6f))
                    .width(1.dp)
                    .height(24.dp)
            )

            Image(
                // TODO: this is themed...
                painter = painterResource(R.drawable.ellipsis_vertical),
                contentDescription = null,
                colorFilter = ColorFilter.tint(rippleColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clickable(onClick = onMore)
                    .size(20.dp)
            )
        }
    }
}
