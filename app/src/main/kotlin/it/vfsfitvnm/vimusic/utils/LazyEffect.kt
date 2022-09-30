@file:OptIn(InternalComposeApi::class)

package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch



@Composable
@NonRestartableComposable
fun lazyEffect(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
): () -> Unit {
    val applyContext = currentComposer.applyCoroutineContext

    val lazyEffect = remember(key1) {
        LazyEffectImpl(applyContext, block)
    }

    return lazyEffect::calculate
}

class LazyEffectImpl(
    parentCoroutineContext: CoroutineContext,
    private val task: suspend CoroutineScope.() -> Unit
) : RememberObserver {
    private val scope = CoroutineScope(parentCoroutineContext)
    private var job: Job? = null

    fun calculate() {
        if (job == null) {
            job = scope.launch(block = task)
        }
    }

    override fun onRemembered() = Unit

    override fun onForgotten() {
        job?.cancel()
        job = null
    }

    override fun onAbandoned() {
        job?.cancel()
        job = null
    }
}
