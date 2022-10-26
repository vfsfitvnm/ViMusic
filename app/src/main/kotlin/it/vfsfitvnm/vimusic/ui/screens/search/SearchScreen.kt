package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import it.vfsfitvnm.compose.persist.PersistMapCleanup
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.secondary

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchScreen(
    initialTextInput: String,
    onSearch: (String) -> Unit,
    onViewPlaylist: (String) -> Unit
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabChanged) = rememberSaveable {
        mutableStateOf(0)
    }

    val (textFieldValue, onTextFieldValueChanged) = rememberSaveable(
        initialTextInput,
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    PersistMapCleanup(tagPrefix = "search/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->
                Box {
                    AnimatedVisibility(
                        visible = textFieldValue.text.isEmpty(),
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300)),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                    ) {
                        BasicText(
                            text = "Enter a name",
                            maxLines = 1,
                            style = LocalAppearance.current.typography.xxl.secondary
                        )
                    }

                    innerTextField()
                }
            }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Online", R.drawable.globe)
                    Item(1, "Library", R.drawable.library)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> OnlineSearch(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            onSearch = onSearch,
                            onViewPlaylist = onViewPlaylist,
                            decorationBox = decorationBox
                        )

                        1 -> LocalSongSearch(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            decorationBox = decorationBox
                        )
                    }
                }
            }
        }
    }
}
