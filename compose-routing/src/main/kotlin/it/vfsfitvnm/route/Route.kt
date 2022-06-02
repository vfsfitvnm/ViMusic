package it.vfsfitvnm.route

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
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
        var listener: ((Route) -> Unit)? = null
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

@Stable
class Route0(
    tag: String
) : Route(tag) {
    context(RouteHandlerScope)
    @Composable
    inline operator fun invoke(content: @Composable () -> Unit) {
        if (this == route) {
            content()
        }
    }

    fun global() {
        GlobalEmitter.listener?.invoke(this)
    }
}

@Stable
class Route1<P0>(
    tag: String,
    state0: MutableState<P0>
) : Route(tag) {
    var p0 by state0

    context(RouteHandlerScope)
    @Composable
    inline operator fun invoke(content: @Composable (P0) -> Unit) {
        if (this == route) {
            if (route is Route1<*>) {
                @Suppress("UNCHECKED_CAST")
                (route as Route1<P0>).let { route ->
                    this.p0 = route.p0
                }
            }
            content(this.p0)
        }
    }

    fun global(p0: P0 = this.p0) {
        this.p0 = p0
        GlobalEmitter.listener?.invoke(this)
    }
}

@Stable
class Route2<P0, P1>(
    tag: String,
    state0: MutableState<P0>,
    state1: MutableState<P1>
) : Route(tag) {
    var p0 by state0
    var p1 by state1

    context(RouteHandlerScope)
    @Composable
    inline operator fun invoke(content: @Composable (P0, P1) -> Unit) {
        if (this == route) {
            if (route is Route2<*, *>) {
                @Suppress("UNCHECKED_CAST")
                (route as Route2<P0, P1>).let { route ->
                    this.p0 = route.p0
                    this.p1 = route.p1
                }
            }
            content(this.p0, this.p1)
        }
    }

    fun global(p0: P0 = this.p0, p1: P1 = this.p1) {
        this.p0 = p0
        this.p1 = p1
        GlobalEmitter.listener?.invoke(this)
    }
}
