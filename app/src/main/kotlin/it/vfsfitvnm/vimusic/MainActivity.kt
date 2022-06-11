package it.vfsfitvnm.vimusic

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.common.util.concurrent.ListenableFuture
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.services.PlayerService
import it.vfsfitvnm.vimusic.ui.components.BottomSheetMenu
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.rememberMenuState
import it.vfsfitvnm.vimusic.ui.screens.HomeScreen
import it.vfsfitvnm.vimusic.ui.screens.IntentUriScreen
import it.vfsfitvnm.vimusic.ui.styling.*
import it.vfsfitvnm.vimusic.ui.views.PlayerView
import it.vfsfitvnm.vimusic.utils.*


@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalTextApi
class MainActivity : ComponentActivity() {
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    private var uri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(this, ComponentName(this, PlayerService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        uri = intent?.data

        setContent {
            val preferences = rememberPreferences()
            val systemUiController = rememberSystemUiController()

            val (isDarkTheme, colorPalette) = when (preferences.colorPaletteMode) {
                ColorPaletteMode.Light -> false to LightColorPalette
                ColorPaletteMode.Dark -> true to DarkColorPalette
                ColorPaletteMode.Black -> true to BlackColorPalette
                ColorPaletteMode.System -> when (isSystemInDarkTheme()) {
                    true -> true to DarkColorPalette
                    false -> false to LightColorPalette
                }
            }

            val rippleTheme = remember(colorPalette.text, isDarkTheme) {
                object : RippleTheme {
                    @Composable
                    override fun defaultColor(): Color = RippleTheme.defaultRippleColor(
                        contentColor = colorPalette.text,
                        lightTheme = !isDarkTheme
                    )

                    @Composable
                    override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
                        contentColor = colorPalette.text,
                        lightTheme = !isDarkTheme
                    )
                }
            }

            val shimmerTheme = remember {
                defaultShimmerTheme.copy(
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = LinearEasing,
                            delayMillis = 250,
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    shaderColors = listOf(
                        Color.Unspecified.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.50f),
                        Color.Unspecified.copy(alpha = 0.25f),
                    ),
                )
            }

            SideEffect {
                systemUiController.setSystemBarsColor(colorPalette.background, !isDarkTheme)
            }

            CompositionLocalProvider(
                LocalOverScrollConfiguration provides null,
                LocalIndication provides rememberRipple(bounded = false),
                LocalRippleTheme provides rippleTheme,
                LocalPreferences provides preferences,
                LocalColorPalette provides colorPalette,
                LocalShimmerTheme provides shimmerTheme,
                LocalTypography provides rememberTypography(colorPalette.text),
                LocalYoutubePlayer provides rememberYoutubePlayer(mediaControllerFuture) {
                    it.repeatMode = preferences.repeatMode
                },
                LocalMenuState provides rememberMenuState(),
                LocalHapticFeedback provides rememberHapticFeedback()
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorPalette.background)
                ) {
                    uri?.let {
                        IntentUriScreen(uri = it)
                    } ?: HomeScreen()

                    PlayerView(
                        layoutState = rememberBottomSheetState(
                            lowerBound = 64.dp, upperBound = maxHeight
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )

                    BottomSheetMenu(
                        state = LocalMenuState.current,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        uri = intent?.data
    }

    override fun onDestroy() {
        MediaController.releaseFuture(mediaControllerFuture)
        super.onDestroy()
    }
}
