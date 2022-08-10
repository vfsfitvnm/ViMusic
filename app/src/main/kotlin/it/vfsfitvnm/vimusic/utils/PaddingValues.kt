package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun PaddingValues.add(bottom: Dp = 0.dp): PaddingValues {
    return object : PaddingValues by this {
        override fun calculateBottomPadding(): Dp = this@add.calculateBottomPadding() + bottom
    }
}
