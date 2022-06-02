package it.vfsfitvnm.route

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
class RouteHandlerScope(
    val route: Route?,
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

    operator fun Route0.invoke() {
        push(this)
    }

    operator fun <P0> Route1<P0>.invoke(
        p0: P0 = this.p0
    ) {
        this.p0 = p0
        push(this)
    }

    operator fun <P0, P1> Route2<P0, P1>.invoke(
        p0: P0 = this.p0,
        p1: P1 = this.p1
    ) {
        this.p0 = p0
        this.p1 = p1
        push(this)
    }
}
