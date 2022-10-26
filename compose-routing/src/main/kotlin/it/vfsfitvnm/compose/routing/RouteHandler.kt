package it.vfsfitvnm.compose.routing

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun RouteHandler(
    modifier: Modifier = Modifier,
    listenToGlobalEmitter: Boolean = false,
    handleBackPress: Boolean = true,
    transitionSpec: AnimatedContentScope<RouteHandlerScope>.() -> ContentTransform = {
        when {
            isStacking -> defaultStacking
            isStill -> defaultStill
            else -> defaultUnstacking
        }
    },
    content: @Composable RouteHandlerScope.() -> Unit
) {
    var route by rememberSaveable(stateSaver = Route.Saver) {
        mutableStateOf(null)
    }

    RouteHandler(
        route = route,
        onRouteChanged = { route = it },
        listenToGlobalEmitter = listenToGlobalEmitter,
        handleBackPress = handleBackPress,
        transitionSpec = transitionSpec,
        modifier = modifier,
        content = content
    )
}

@ExperimentalAnimationApi
@Composable
fun RouteHandler(
    route: Route?,
    onRouteChanged: (Route?) -> Unit,
    modifier: Modifier = Modifier,
    listenToGlobalEmitter: Boolean = false,
    handleBackPress: Boolean = true,
    transitionSpec: AnimatedContentScope<RouteHandlerScope>.() -> ContentTransform = {
        when {
            isStacking -> defaultStacking
            isStill -> defaultStill
            else -> defaultUnstacking
        }
    },
    content: @Composable RouteHandlerScope.() -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val parameters = rememberSaveable {
        arrayOfNulls<Any?>(2)
    }

    val scope = remember(route) {
        RouteHandlerScope(
            route = route,
            parameters = parameters,
            push = onRouteChanged,
            pop = { if (handleBackPress) backDispatcher?.onBackPressed() else onRouteChanged(null) }
        )
    }

    if (listenToGlobalEmitter && route == null) {
        OnGlobalRoute { (newRoute, newParameters) ->
            newParameters.forEachIndexed(parameters::set)
            onRouteChanged(newRoute)
        }
    }

    BackHandler(enabled = handleBackPress && route != null) {
        onRouteChanged(null)
    }

    updateTransition(targetState = scope, label = null).AnimatedContent(
        transitionSpec = transitionSpec,
        contentKey = RouteHandlerScope::route,
        modifier = modifier,
    ) {
        it.content()
    }
}
