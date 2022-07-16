package it.vfsfitvnm.vimusic

import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import it.vfsfitvnm.vimusic.enums.ColorPaletteMode
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.service.PlayerService
import it.vfsfitvnm.vimusic.ui.components.BottomSheetMenu
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.rememberBottomSheetState
import it.vfsfitvnm.vimusic.ui.components.rememberMenuState
import it.vfsfitvnm.vimusic.ui.screens.HomeScreen
import it.vfsfitvnm.vimusic.ui.screens.IntentUriScreen
import it.vfsfitvnm.vimusic.ui.styling.Appearance
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.views.PlayerView
import it.vfsfitvnm.vimusic.utils.*


class MainActivity : ComponentActivity() {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) {
                this@MainActivity.binder = service
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    private var binder by mutableStateOf<PlayerService.Binder?>(null)
    private var uri by mutableStateOf<Uri?>(null, neverEqualPolicy())

    override fun onStart() {
        super.onStart()
        bindService(intent<PlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    @SuppressLint("BatteryLife")
    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
        ExperimentalTextApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = intent?.data

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()

            var appearance by remember(isSystemInDarkTheme) {
                with(preferences) {
                    val colorPaletteMode = getEnum(colorPaletteModeKey, ColorPaletteMode.System)
                    val thumbnailRoundness = getEnum(thumbnailRoundnessKey, ThumbnailRoundness.Light)

                    mutableStateOf(
                        Appearance(
                            colorPalette = colorPaletteMode.palette(isSystemInDarkTheme),
                            typography = colorPaletteMode.typography(isSystemInDarkTheme),
                            thumbnailShape = thumbnailRoundness.shape()
                        )
                    )
                }
            }

            DisposableEffect(isSystemInDarkTheme) {
                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                        when (key) {
                            colorPaletteModeKey -> {
                                val colorPaletteMode = sharedPreferences.getEnum(key, ColorPaletteMode.System)

                                appearance = appearance.copy(
                                    colorPalette = colorPaletteMode.palette(isSystemInDarkTheme),
                                    typography = colorPaletteMode.typography(isSystemInDarkTheme),
                                )
                            }
                            thumbnailRoundnessKey -> {
                                val thumbnailRoundness = sharedPreferences.getEnum(key, ThumbnailRoundness.Light)

                                appearance = appearance.copy(
                                    thumbnailShape = thumbnailRoundness.shape()
                                )
                            }
                        }
                    }

                with(preferences) {
                    registerOnSharedPreferenceChangeListener(listener)

                    onDispose {
                        unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }
            }

            val systemUiController = rememberSystemUiController()

            val rippleTheme = remember(appearance.colorPalette.text, appearance.colorPalette.isDark) {
                object : RippleTheme {
                    @Composable
                    override fun defaultColor(): Color = RippleTheme.defaultRippleColor(
                        contentColor = appearance.colorPalette.text,
                        lightTheme = !appearance.colorPalette.isDark
                    )

                    @Composable
                    override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
                        contentColor = appearance.colorPalette.text,
                        lightTheme = !appearance.colorPalette.isDark
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
                systemUiController.setSystemBarsColor(appearance.colorPalette.background, !appearance.colorPalette.isDark)
            }

            CompositionLocalProvider(
                LocalAppearance provides appearance,
                LocalOverscrollConfiguration provides null,
                LocalIndication provides rememberRipple(bounded = false),
                LocalRippleTheme provides rippleTheme,
                LocalShimmerTheme provides shimmerTheme,
                LocalPlayerServiceBinder provides binder,
                LocalMenuState provides rememberMenuState(),
                LocalHapticFeedback provides rememberHapticFeedback()
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appearance.colorPalette.background)
                ) {
                    when (val uri = uri) {
                        null -> {
                            HomeScreen()

                            PlayerView(
                                layoutState = rememberBottomSheetState(
                                    lowerBound = Dimensions.collapsedPlayer, upperBound = maxHeight
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                            )
                        }
                        else -> IntentUriScreen(uri = uri)
                    }

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
}

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }
