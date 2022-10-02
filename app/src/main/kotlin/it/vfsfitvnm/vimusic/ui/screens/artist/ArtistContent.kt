package it.vfsfitvnm.vimusic.ui.screens.artist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.savers.ListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.produceSaveableRelaunchableOneShotState
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.youtubemusic.Innertube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
inline fun <T : Innertube.Item> ArtistContent(
    artist: Artist?,
    youtubeArtistPage: Innertube.ArtistPage?,
    isLoading: Boolean,
    isError: Boolean,
    stateSaver: ListSaver<T, List<Any?>>,
    crossinline itemsPageProvider: suspend (String?) -> Result<Innertube.ItemsPage<T>?>?,
    crossinline bookmarkIconContent: @Composable () -> Unit,
    crossinline shareIconContent: @Composable () -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemShimmer: @Composable () -> Unit,
) {
    val (_, typography) = LocalAppearance.current

    var items by rememberSaveable(stateSaver = stateSaver) {
        mutableStateOf(listOf())
    }

    var isLoadingItems by remember {
        mutableStateOf(false)
    }

    var isErrorItems by remember {
        mutableStateOf(false)
    }

    val (continuationState, fetch) = produceSaveableRelaunchableOneShotState(
        initialValue = null,
        stateSaver = autoSaver<String?>(),
        youtubeArtistPage
    ) {
        if (youtubeArtistPage == null) return@produceSaveableRelaunchableOneShotState

        println("loading... $value")

        isLoadingItems = true
        withContext(Dispatchers.IO) {
            itemsPageProvider(value)?.onSuccess { itemsPage ->
                value = itemsPage?.continuation
                itemsPage?.items?.let {
                    items = items.plus(it).distinctBy(Innertube.Item::key)
                }
                isErrorItems = false
                isLoadingItems = false
            }?.onFailure {
                println("error (2): $it")
                isErrorItems = true
                isLoadingItems = false
            }
        }
    }

    val continuation by continuationState

    when {
        artist != null -> {
            LazyColumn(
                contentPadding = LocalPlayerAwarePaddingValues.current,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0,
                ) {
                    Header(title = artist.name ?: "Unknown") {
                        bookmarkIconContent()
                        shareIconContent()
                    }
                }

                items(
                    items = items,
                    key = Innertube.Item::key,
                    itemContent = itemContent
                )

                if (isError || isErrorItems) {
                    item(key = "error") {
                        BasicText(
                            text = "An error has occurred",
                            style = LocalAppearance.current.typography.s.secondary.center,
                            modifier = Modifier
                                .padding(all = 16.dp)
                        )
                    }
                } else {
                    item("loading") {
                        val hasMore = continuation != null

                        if (hasMore || items.isEmpty()) {
                            ShimmerHost {
                                repeat(if (hasMore) 3 else 8) {
                                    itemShimmer()
                                }
                            }

//                            if (hasMore && items.isNotEmpty()) {
//                                println("loading again!")
//                                SideEffect(fetch)
//                            }
                        }
                    }
                }
            }
        }
        isError -> BasicText(
            text = "An error has occurred",
            style = LocalAppearance.current.typography.s.secondary.center,
            modifier = Modifier
                .padding(all = 16.dp)
        )
        isLoading -> ShimmerHost {
            HeaderPlaceholder()

            repeat(5) {
                itemShimmer()
            }
        }
    }
}
