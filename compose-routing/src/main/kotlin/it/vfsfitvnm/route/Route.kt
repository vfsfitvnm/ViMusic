@file:Suppress("UNCHECKED_CAST")

package it.vfsfitvnm.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable

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

    object GlobalEmitter {
        var listener: ((Route, Array<Any?>) -> Unit)? = null
    }

    object Saver : androidx.compose.runtime.saveable.Saver<Route?, String> {
        override fun restore(value: String): Route? = value.takeIf(String::isNotEmpty)?.let(::Route)
        override fun SaverScope.save(value: Route?): String = value?.tag ?: ""
    }
}

@Composable
fun rememberRoute(route: Route? = null): MutableState<Route?> {
    return rememberSaveable(stateSaver = Route.Saver) {
        mutableStateOf(route)
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
        GlobalEmitter.listener?.invoke(this, emptyArray())
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
        GlobalEmitter.listener?.invoke(this, arrayOf(p0))
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
        GlobalEmitter.listener?.invoke(this, arrayOf(p0, p1))
    }
}
