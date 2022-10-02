package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.savers.ListSaver
import it.vfsfitvnm.vimusic.savers.resultSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.produceSaveableRelaunchableOneShotState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.MusicShelfRenderer
import it.vfsfitvnm.youtubemusic.models.bodies.ContinuationBody
import it.vfsfitvnm.youtubemusic.models.bodies.SearchBody
import it.vfsfitvnm.youtubemusic.requests.searchPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
inline fun <T : Innertube.Item> SearchResult(
    query: String,
    filter: String,
    stateSaver: ListSaver<T, List<Any?>>,
    noinline fromMusicShelfRendererContent: (MusicShelfRenderer.Content) -> T?,
    crossinline onSearchAgain: () -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable BoxScope.() -> Unit,
) {
    val (_, typography) = LocalAppearance.current

    var items by rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(listOf())
    }

    val (continuationResultState, fetch) = produceSaveableRelaunchableOneShotState(
        initialValue = null,
        stateSaver = resultSaver(autoSaver<String?>())
    ) {
        val token = value?.getOrNull()

        value = null

        value = withContext(Dispatchers.IO) {
            if (token == null) {
                Innertube.searchPage(
                    body = SearchBody(query = query, params = filter),
                    fromMusicShelfRendererContent = fromMusicShelfRendererContent
                )
            } else {
                Innertube.searchPage(
                    body = ContinuationBody(continuation = token),
                    fromMusicShelfRendererContent = fromMusicShelfRendererContent
                )
            }
        }?.map { itemsPage ->
            itemsPage?.items?.let {
                items = items.plus(it).distinctBy(Innertube.Item::key)
            }

            itemsPage?.continuation
        }
    }

    val continuationResult by continuationResultState
    
    LazyColumn(
        contentPadding = LocalPlayerAwarePaddingValues.current,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0,
        ) {
            Header(
                title = query,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onSearchAgain()
                        }
                    }
            )
        }

        items(
            items = items,
            key = Innertube.Item::key,
            itemContent = itemContent
        )

        continuationResult?.getOrNull()?.let {
            if (items.isNotEmpty()) {
                item {
                    SideEffect(fetch)
                }
            }
        } ?: continuationResult?.exceptionOrNull()?.let {
            item {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                fetch()
                            }
                        }
                        .fillMaxSize()
                ) {
                    BasicText(
                        text = "An error has occurred.\nTap to retry",
                        style = typography.s.medium.secondary.center,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        } ?: continuationResult?.let {
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        BasicText(
                            text = "No results found.\nPlease try a different query or category",
                            style = typography.s.medium.secondary.center,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        } ?: item(key = "loading") {
            Column(
                modifier = Modifier
                    .shimmer()
            ) {
                repeat(if (items.isEmpty()) 8 else 3) { index ->
                    Box(
                        modifier = Modifier
                            .alpha(1f - index * 0.125f),
                        content = itemPlaceholderContent
                    )
                }
            }
        }
    }
}
