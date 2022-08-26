package it.vfsfitvnm.vimusic.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

val LocalMenuState = staticCompositionLocalOf { MenuState() }

@Stable
class MenuState {
    var isDisplayed by mutableStateOf(false)
        private set

    var content by mutableStateOf<@Composable () -> Unit>({})
        private set

    fun display(content: @Composable () -> Unit) {
        this.content = content
        isDisplayed = true
    }

    fun hide() {
        isDisplayed = false
    }
}

@Composable
fun BottomSheetMenu(
    state: MenuState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isDisplayed,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BackHandler(onBack = state::hide)

        Spacer(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures {
                        state.hide()
                    }
                }
                .background(Color.Black.copy(alpha = 0.5f))
                .fillMaxSize()
        )
    }

    AnimatedVisibility(
        visible = state.isDisplayed,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        state.content()
    }
}
