package it.vfsfitvnm.vimusic.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberLazyListStates(count: Int): List<LazyListState> {
    return rememberSaveable(
        saver = listSaver(
            save = { states: List<LazyListState> ->
                List(states.size * 2) {
                    when (it % 2) {
                        0 -> states[it / 2].firstVisibleItemIndex
                        1 -> states[it / 2].firstVisibleItemScrollOffset
                        else -> error("unreachable")
                    }
                }
            },
            restore = { states ->
                List(states.size / 2) {
                    LazyListState(
                        firstVisibleItemIndex = states[it * 2],
                        firstVisibleItemScrollOffset = states[it * 2 + 1]
                    )
                }
            }
        )
    ) {
        List(count) { LazyListState(0, 0) }
    }
}

@Composable
fun rememberLazyGridStates(count: Int): List<LazyGridState> {
    return rememberSaveable(
        saver = listSaver(
            save = { states: List<LazyGridState> ->
                List(states.size * 2) {
                    when (it % 2) {
                        0 -> states[it / 2].firstVisibleItemIndex
                        1 -> states[it / 2].firstVisibleItemScrollOffset
                        else -> error("unreachable")
                    }
                }
            },
            restore = { states ->
                List(states.size / 2) {
                    LazyGridState(
                        firstVisibleItemIndex = states[it * 2],
                        firstVisibleItemScrollOffset = states[it * 2 + 1]
                    )
                }
            }
        )
    ) {
        List(count) { LazyGridState(0, 0) }
    }
}
