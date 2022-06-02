package it.vfsfitvnm.vimusic.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

val LocalMenuState = compositionLocalOf<MenuState> { TODO() }

class MenuState(isDisplayedState: MutableState<Boolean>) {
    var isDisplayed by isDisplayedState
        private set

    var content: @Composable () -> Unit = {}

    fun display(content: @Composable () -> Unit) {
        this.content = content
        isDisplayed = true
    }

    fun hide() {
        isDisplayed = false
    }
}

@Composable
fun rememberMenuState(): MenuState {
    val isDisplayedState = remember {
        mutableStateOf(false)
    }

    return remember {
        MenuState(
            isDisplayedState = isDisplayedState
        )
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