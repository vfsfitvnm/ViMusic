package it.vfsfitvnm.route

import androidx.compose.animation.*
import androidx.compose.animation.core.tween


@ExperimentalAnimationApi
val AnimatedContentScope<RouteHandlerScope>.leftSlide: ContentTransform
    get() = slideIntoContainer(AnimatedContentScope.SlideDirection.Left) with
            slideOutOfContainer(AnimatedContentScope.SlideDirection.Left)

@ExperimentalAnimationApi
val AnimatedContentScope<RouteHandlerScope>.rightSlide: ContentTransform
    get() = slideIntoContainer(AnimatedContentScope.SlideDirection.Right) with
            slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)

@ExperimentalAnimationApi
val AnimatedContentScope<RouteHandlerScope>.fastFade: ContentTransform
    get() = fadeIn(tween(200)) with fadeOut(tween(200))

@ExperimentalAnimationApi
val AnimatedContentScope<RouteHandlerScope>.empty: ContentTransform
    get() = EnterTransition.None with ExitTransition.None
