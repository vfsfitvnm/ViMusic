package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import it.vfsfitvnm.vimusic.savers.ListSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn


@Composable
fun <T> produceSaveableListState(
    flowProvider: () -> Flow<List<T>>,
    stateSaver: ListSaver<T, List<Any?>>,
): State<List<T>> {
    val state = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(emptyList())
    }

    var hasToRecollect by rememberSaveable {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        flowProvider()
            .flowOn(Dispatchers.IO)
            .drop(if (hasToRecollect) 0 else 1)
            .collect {
                hasToRecollect = false
                state.value = it
            }
    }

    return state
}

@Composable
fun <T> produceSaveableListState(
    flowProvider: () -> Flow<List<T>>,
    stateSaver: ListSaver<T, List<Any?>>,
    key1: Any?,
): State<List<T>> {
    val state = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(emptyList())
    }

    var hasToRecollect by rememberSaveable(key1) {
//        println("hasToRecollect: $sortBy, $sortOrder")
        mutableStateOf(true)
    }

    LaunchedEffect(key1) {
//        println("[${System.currentTimeMillis()}] LaunchedEffect, $hasToRecollect, $sortBy, $sortOrder")
        flowProvider()
            .flowOn(Dispatchers.IO)
            .drop(if (hasToRecollect) 0 else 1)
            .collect {
                hasToRecollect = false
//                println("[${System.currentTimeMillis()}] collecting... ")
                state.value = it
            }
    }

    return state
}

@Composable
fun <T> produceSaveableListState(
    flowProvider: () -> Flow<List<T>>,
    stateSaver: ListSaver<T, List<Any?>>,
    key1: Any?,
    key2: Any?,
): State<List<T>> {
    val state = rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(emptyList())
    }

//    var hasToRecollect by rememberSaveable(key1, key2) {
////        println("hasToRecollect: $sortBy, $sortOrder")
//        mutableStateOf(true)
//    }

    LaunchedEffect(key1, key2) {
//        println("[${System.currentTimeMillis()}] LaunchedEffect, $hasToRecollect, $sortBy, $sortOrder")
        flowProvider()
            .flowOn(Dispatchers.IO)
//            .drop(if (hasToRecollect) 0 else 1)
            .collect {
//                hasToRecollect = false
//                println("[${System.currentTimeMillis()}] collecting... ")
                state.value = it
            }
    }

    return state
}
