
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(InternalComposeApi::class)

package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.*
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
