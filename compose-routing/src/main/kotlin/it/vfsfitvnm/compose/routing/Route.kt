@file:Suppress("UNCHECKED_CAST")

package it.vfsfitvnm.compose.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Immutable
open class Route internal constructor(val tag: String) {
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is Route -> tag == other.tag
            else -> false
        }
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

    object Saver : androidx.compose.runtime.saveable.Saver<Route?, String> {
        override fun restore(value: String): Route? = value.takeIf(String::isNotEmpty)?.let(::Route)
        override fun SaverScope.save(value: Route?): String = value?.tag ?: ""
    }
}

@Immutable
class Route0(tag: String) : Route(tag) {
    context(RouteHandlerScope)
    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        if (this == route) {
            content()
        }
    }

    fun global() {
        globalRouteFlow.tryEmit(this to emptyArray())
    }
}

@Immutable
class Route1<P0>(tag: String) : Route(tag) {
    context(RouteHandlerScope)
    @Composable
    operator fun invoke(content: @Composable (P0) -> Unit) {
        if (this == route) {
            content(parameters[0] as P0)
        }
    }

    fun global(p0: P0) {
        globalRouteFlow.tryEmit(this to arrayOf(p0))
    }

    suspend fun ensureGlobal(p0: P0) {
        globalRouteFlow.subscriptionCount.filter { it > 0 }.first()
        globalRouteFlow.emit(this to arrayOf(p0))
    }
}

@Immutable
class Route2<P0, P1>(tag: String) : Route(tag) {
    context(RouteHandlerScope)
    @Composable
    operator fun invoke(content: @Composable (P0, P1) -> Unit) {
        if (this == route) {
            content(parameters[0] as P0, parameters[1] as P1)
        }
    }

    fun global(p0: P0, p1: P1) {
        globalRouteFlow.tryEmit(this to arrayOf(p0, p1))
    }
}
