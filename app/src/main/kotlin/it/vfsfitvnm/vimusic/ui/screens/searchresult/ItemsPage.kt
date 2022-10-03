package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.ui.components.themed.ShimmerHost
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.utils.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
inline fun <T : Innertube.Item> ItemsPage(
    stateSaver: Saver<Innertube.ItemsPage<T>, List<Any?>>,
    noinline itemsPageProvider: (suspend (String?) -> Result<Innertube.ItemsPage<T>?>?)? = null,
    crossinline headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val updatedItemsPageProvider by rememberUpdatedState(itemsPageProvider)

    val itemsPage by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(stateSaver),
        lazyListState, updatedItemsPageProvider
    ) {
        val currentItemsPageProvider = updatedItemsPageProvider ?: return@produceSaveableState

        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" } }
            .collect { shouldLoadMore ->
                if (!shouldLoadMore) return@collect

                withContext(Dispatchers.IO) {
                    currentItemsPageProvider(value?.continuation)
                }?.onSuccess {
                    if (it == null) {
                        if (value == null) {
                            value = Innertube.ItemsPage(null, null)
                        }
                    } else {
                        value += it
                    }
                }
            }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = LocalPlayerAwarePaddingValues.current,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0,
        ) {
            headerContent(null)
        }

        items(
            items = itemsPage?.items ?: emptyList(),
            key = Innertube.Item::key,
            itemContent = itemContent
        )

        if (!(itemsPage != null && itemsPage?.continuation == null)) {
            item(key = "loading") {
                ShimmerHost {
                    repeat(if (itemsPage?.items.isNullOrEmpty()) 8 else 3) {
                        itemPlaceholderContent()
                    }
                }
            }
        }
    }
}
