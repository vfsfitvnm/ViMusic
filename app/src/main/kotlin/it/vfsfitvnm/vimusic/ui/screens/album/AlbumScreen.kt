package it.vfsfitvnm.vimusic.ui.screens.album

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes

//@Stable
//class AlbumScreenState(
//    initialIsLoading: Boolean = false,
//    initialError: Throwable? = null,
//    initialAlbum: Album? = null,
//    initialYouTubeAlbum: YouTube.PlaylistOrAlbum? = null,
//) {
//    var isLoading by mutableStateOf(initialIsLoading)
//    var error by mutableStateOf(initialError)
//    var album by mutableStateOf(initialAlbum)
//    var youtubeAlbum by mutableStateOf(initialYouTubeAlbum)
//
//    suspend fun loadAlbum(browseId: String) {
//        println("loadAlbum $browseId")
//        Database.album(browseId).flowOn(Dispatchers.IO).collect {
//            if (it == null) {
//                loadYouTubeAlbum(browseId)
//            } else {
//                album = it
//            }
//        }
//    }
//
//    suspend fun loadYouTubeAlbum(browseId: String) {
//        println("loadYouTubeAlbum $browseId")
//        if (youtubeAlbum == null) {
//            isLoading = true
//            withContext(Dispatchers.IO) {
//                YouTube.album(browseId)
//            }?.onSuccess {
//                youtubeAlbum = it
//                isLoading = false
//
//                query {
//                    Database.upsert(
//                        Album(
//                            id = browseId,
//                            title = it.title,
//                            thumbnailUrl = it.thumbnail?.url,
//                            year = it.year,
//                            authorsText = it.authors?.joinToString(
//                                "",
//                                transform = YouTube.Info<NavigationEndpoint.Endpoint.Browse>::name
//                            ),
//                            shareUrl = it.url,
//                            timestamp = System.currentTimeMillis()
//                        ),
//                        it.items?.mapIndexedNotNull { position, albumItem ->
//                            albumItem.toMediaItem(browseId, it)?.let { mediaItem ->
//                                Database.insert(mediaItem)
//                                SongAlbumMap(
//                                    songId = mediaItem.mediaId,
//                                    albumId = browseId,
//                                    position = position
//                                )
//                            }
//                        } ?: emptyList()
//                    )
//                }
//
//            }?.onFailure {
//                error = it
//                isLoading = false
//            }
//        }
//    }
//}
//
//object AlbumScreenStateSaver : Saver<AlbumScreenState, List<Any?>> {
//    override fun restore(value: List<Any?>) = AlbumScreenState(
//        initialIsLoading = value[0] as Boolean,
//        initialError = value[1] as Throwable?,
//        initialAlbum = (value[1] as List<Any?>?)?.let(AlbumSaver::restore),
//    )
//
//    override fun SaverScope.save(value: AlbumScreenState): List<Any?> =
//        listOf(
//            value.isLoading,
//            value.error,
//            value.album?.let { with(AlbumSaver) { save(it) } },
////            value.youtubeAlbum?.let { with(YouTubeAlbumSaver) { save(it) } },
//        )
//}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun AlbumScreen(browseId: String) {
    val saveableStateHolder = rememberSaveableStateHolder()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChanged = { },
                tabColumnContent = { Item ->
                    Item(0, "Overview", R.drawable.sparkles)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    AlbumOverview(browseId = browseId)
                }
            }
        }
    }
}
