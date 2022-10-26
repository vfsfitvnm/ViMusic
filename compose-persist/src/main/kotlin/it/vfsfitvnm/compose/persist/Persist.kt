package it.vfsfitvnm.compose.persist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Suppress("UNCHECKED_CAST")
@Composable
fun <T> persist(tag: String, initialValue: T): MutableState<T> {
    val context = LocalContext.current

    return remember {
        context.persistMap?.getOrPut(tag) { mutableStateOf(initialValue) } as? MutableState<T>
            ?: mutableStateOf(initialValue)
    }
}

@Composable
fun <T> persistList(tag: String): MutableState<List<T>> =
    persist(tag = tag, initialValue = emptyList())

@Composable
fun <T : Any?> persist(tag: String): MutableState<T?> =
    persist(tag = tag, initialValue = null)
