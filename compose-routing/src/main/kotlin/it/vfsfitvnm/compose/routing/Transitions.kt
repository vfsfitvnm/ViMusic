package it.vfsfitvnm.compose.routing

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut

@ExperimentalAnimationApi
val defaultStacking = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

@ExperimentalAnimationApi
val defaultUnstacking = ContentTransform(
    initialContentExit = fadeOut(),
    targetContentEnter = EnterTransition.None,
    targetContentZIndex = 0f
)

@ExperimentalAnimationApi
val defaultStill = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

@ExperimentalAnimationApi
inline val AnimatedContentScope<RouteHandlerScope>.isStacking: Boolean
    get() = initialState.route == null && targetState.route != null

@ExperimentalAnimationApi
inline val AnimatedContentScope<RouteHandlerScope>.isUnstacking: Boolean
    get() = initialState.route != null && targetState.route == null

@ExperimentalAnimationApi
inline val AnimatedContentScope<RouteHandlerScope>.isStill: Boolean
    get() = initialState.route == null && targetState.route == null

@ExperimentalAnimationApi
inline val AnimatedContentScope<RouteHandlerScope>.isUnknown: Boolean
    get() = initialState.route != null && targetState.route != null
