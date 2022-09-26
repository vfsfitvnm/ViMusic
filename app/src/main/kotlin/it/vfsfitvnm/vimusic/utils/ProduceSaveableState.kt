package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalTypeInference::class)
@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable(stateSaver = stateSaver) { mutableStateOf(initialValue) }

    var hasToFetch by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (hasToFetch) {
            ProduceSaveableStateScope(result, coroutineContext).producer()
            hasToFetch = false
        }
    }
    return result
}

@OptIn(ExperimentalTypeInference::class)
@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    key1: Any?,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable(stateSaver = stateSaver) { mutableStateOf(initialValue) }

    var hasToFetch by rememberSaveable(key1) { mutableStateOf(true) }

    LaunchedEffect(key1) {
        if (hasToFetch) {
            ProduceSaveableStateScope(result, coroutineContext).producer()
            hasToFetch = false
        }
    }
    return result
}

@OptIn(ExperimentalTypeInference::class)
@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    key1: Any?,
    key2: Any?,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable(stateSaver = stateSaver) { mutableStateOf(initialValue) }

    var hasToFetch by rememberSaveable(key1, key2) { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (hasToFetch) {
            ProduceSaveableStateScope(result, coroutineContext).producer()
            hasToFetch = false
        }
    }

    return result
}

@OptIn(ExperimentalTypeInference::class)
@Composable
fun <T> produceSaveableRelaunchableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    key1: Any?,
    key2: Any?,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): Pair<State<T>, () -> Unit> {
    val result = rememberSaveable(stateSaver = stateSaver) { mutableStateOf(initialValue) }

    var hasToFetch by rememberSaveable(key1, key2) { mutableStateOf(true) }

    val relaunchableEffect = relaunchableEffect(key1, key2) {
        if (hasToFetch) {
            ProduceSaveableStateScope(result, coroutineContext).producer()
            hasToFetch = false
        }
    }

    return result to {
        hasToFetch = true
        relaunchableEffect()
    }
}

private class ProduceSaveableStateScope<T>(
    state: MutableState<T>,
    override val coroutineContext: CoroutineContext
) : ProduceStateScope<T>, MutableState<T> by state {
    override suspend fun awaitDispose(onDispose: () -> Unit): Nothing {
        try {
            suspendCancellableCoroutine<Nothing> { }
        } finally {
            onDispose()
        }
    }
}
