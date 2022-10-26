package it.vfsfitvnm.compose.routing

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class RouteHandlerScope(
    val route: Route?,
    val parameters: Array<Any?>,
    private val push: (Route?) -> Unit,
    val pop: () -> Unit,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    inline fun host(content: @Composable () -> Unit) {
        if (route == null) {
            content()
        }
    }

    operator fun Route.invoke() {
        push(this)
    }

    operator fun <P0> Route.invoke(p0: P0) {
        parameters[0] = p0
        invoke()
    }

    operator fun <P0, P1> Route.invoke(p0: P0, p1: P1) {
        parameters[1] = p1
        invoke(p0)
    }
}
