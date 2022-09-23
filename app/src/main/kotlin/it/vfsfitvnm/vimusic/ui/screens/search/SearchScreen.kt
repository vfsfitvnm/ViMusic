package it.vfsfitvnm.vimusic.ui.screens.search

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.net.toUri
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchScreen(initialTextInput: String, onSearch: (String) -> Unit, onUri: (Uri) -> Unit) {
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

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val isOpenableUrl = remember(textFieldValue.text) {
                listOf(
                    "https://www.youtube.com/watch?",
                    "https://music.youtube.com/watch?",
                    "https://m.youtube.com/watch?",
                    "https://www.youtube.com/playlist?",
                    "https://music.youtube.com/playlist?",
                    "https://m.youtube.com/playlist?",
                    "https://youtu.be/",
                ).any(textFieldValue.text::startsWith)
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
                        0 -> OnlineSearchTab(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            isOpenableUrl = isOpenableUrl,
                            onSearch = onSearch,
                            onUri = {
                                if (isOpenableUrl) {
                                    onUri(textFieldValue.text.toUri())
                                }
                            }
                        )
                        1 -> LibrarySearchTab(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged
                        )
                    }
                }
            }
        }
    }
}
