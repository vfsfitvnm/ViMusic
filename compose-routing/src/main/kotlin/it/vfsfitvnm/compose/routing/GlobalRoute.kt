package it.vfsfitvnm.compose.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableSharedFlow

internal val globalRouteFlow = MutableSharedFlow<Pair<Route, Array<Any?>>>(extraBufferCapacity = 1)

@Composable
fun OnGlobalRoute(block: suspend (Pair<Route, Array<Any?>>) -> Unit) {
    LaunchedEffect(Unit) {
        globalRouteFlow.collect(block)
    }
}
