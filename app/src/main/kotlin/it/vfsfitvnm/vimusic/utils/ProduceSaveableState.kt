@file:OptIn(ExperimentalTypeInference::class)

package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(initialValue)
    }

    LaunchedEffect(Unit) {
        ProduceSaveableStateScope(result, coroutineContext).producer()
    }

    return result
}

@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    key1: Any?,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val state = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(initialValue)
    }

    LaunchedEffect(key1) {
        ProduceSaveableStateScope(state, coroutineContext).producer()
    }

    return state
}

@Composable
fun <T> produceSaveableState(
    initialValue: T,
    stateSaver: Saver<T, out Any>,
    key1: Any?,
    key2: Any?,
    @BuilderInference producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val result = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(initialValue)
    }

    LaunchedEffect(key1, key2) {
        ProduceSaveableStateScope(result, coroutineContext).producer()
    }

    return result
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
