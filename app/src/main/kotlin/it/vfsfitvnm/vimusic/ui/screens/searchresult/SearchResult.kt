package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.savers.ListSaver
import it.vfsfitvnm.vimusic.savers.StringResultSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.views.SearchResultLoadingOrError
import it.vfsfitvnm.vimusic.utils.produceSaveableRelaunchableOneShotState
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
inline fun <T : YouTube.Item> SearchResult(
    query: String,
    filter: String,
    stateSaver: ListSaver<T, List<Any?>>,
    crossinline onSearchAgain: () -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemShimmer: @Composable BoxScope.() -> Unit,
) {
    var items by rememberSaveable(query, filter, stateSaver = stateSaver) {
        mutableStateOf(listOf())
    }

    val (continuationResultState, fetch) = produceSaveableRelaunchableOneShotState(
        initialValue = null,
        stateSaver = StringResultSaver,
        query, filter
    ) {
        val token = value?.getOrNull()

        value = null

        value = withContext(Dispatchers.IO) {
            YouTube.search(query, filter, token)
        }?.map { searchResult ->
            @Suppress("UNCHECKED_CAST")
            items = items.plus(searchResult.items as List<T>).distinctBy(YouTube.Item::key)
            searchResult.continuation
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
            key = { it.key!! },
            itemContent = itemContent
        )

        continuationResult?.getOrNull()?.let {
            if (items.isNotEmpty()) {
                item {
                    SideEffect(fetch)
                }
            }
        } ?: continuationResult?.exceptionOrNull()?.let { throwable ->
            item {
                SearchResultLoadingOrError(
                    errorMessage = throwable.javaClass.canonicalName,
                    onRetry = fetch,
                    shimmerContent = {}
                )
            }
        } ?: continuationResult?.let {
            if (items.isEmpty()) {
                item {
                    TextCard(icon = R.drawable.sad) {
                        Title(text = "No results found")
                        Text(text = "Please try a different query or category.")
                    }
                }
            }
        } ?: item(key = "loading") {
            SearchResultLoadingOrError(
                itemCount = if (items.isEmpty()) 8 else 3,
                shimmerContent = itemShimmer
            )
        }
    }
}