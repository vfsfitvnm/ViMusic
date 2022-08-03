@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(InternalComposeApi::class)

package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffectImpl
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope

@Composable
@NonRestartableComposable
fun relaunchableEffect(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
): () -> Unit {
    val applyContext = currentComposer.applyCoroutineContext
    val launchedEffect = remember(key1) { LaunchedEffectImpl(applyContext, block) }
    return launchedEffect::onRemembered
}

@Composable
@NonRestartableComposable
fun relaunchableEffect2(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
): RememberObserver {
    val applyContext = currentComposer.applyCoroutineContext
    return remember(key1) { LaunchedEffectImpl(applyContext, block) }
}
