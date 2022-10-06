package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ScrollState.isScrollingDown(): Boolean {
    var previousValue by remember(this) {
        mutableStateOf(value)
    }

    return remember(this) {
        derivedStateOf {
            (previousValue >= value).also {
                previousValue = value
            }
        }
    }.value
}
