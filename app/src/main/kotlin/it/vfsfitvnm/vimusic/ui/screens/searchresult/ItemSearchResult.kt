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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.views.SearchResultLoadingOrError
import it.vfsfitvnm.youtubemusic.YouTube

@ExperimentalAnimationApi
@Composable
inline fun <I : YouTube.Item> ItemSearchResult(
    query: String,
    filter: String,
    crossinline onSearchAgain: () -> Unit,
    viewModel: ItemSearchResultViewModel<I> = viewModel(
        key = query + filter,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ItemSearchResultViewModel<I>(query, filter) as T
            }
        }
    ),
    crossinline itemContent: @Composable LazyItemScope.(I) -> Unit,
    noinline itemShimmer: @Composable BoxScope.() -> Unit,
) {
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
            items = viewModel.items,
            key = { it.key!! },
            itemContent = itemContent
        )

        viewModel.continuationResult?.getOrNull()?.let {
            if (viewModel.items.isNotEmpty()) {
                item {
                    SideEffect(viewModel::fetch)
                }
            }
        } ?: viewModel.continuationResult?.exceptionOrNull()?.let { throwable ->
            item {
                SearchResultLoadingOrError(
                    errorMessage = throwable.javaClass.canonicalName,
                    onRetry = viewModel::fetch,
                    shimmerContent = {}
                )
            }
        } ?: viewModel.continuationResult?.let {
            if (viewModel.items.isEmpty()) {
                item {
                    TextCard(icon = R.drawable.sad) {
                        Title(text = "No results found")
                        Text(text = "Please try a different query or category.")
                    }
                }
            }
        } ?: item(key = "loading") {
            SearchResultLoadingOrError(
                itemCount = if (viewModel.items.isEmpty()) 8 else 3,
                shimmerContent = itemShimmer
            )
        }
    }
}
