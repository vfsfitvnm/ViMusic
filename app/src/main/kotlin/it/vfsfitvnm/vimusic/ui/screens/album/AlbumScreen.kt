package it.vfsfitvnm.vimusic.ui.screens.album

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.savers.AlbumResultSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.toMediaItem
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun AlbumScreen(browseId: String) {
    val saveableStateHolder = rememberSaveableStateHolder()

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val albumResult by produceSaveableState(
                initialValue = null,
                stateSaver = AlbumResultSaver,
            ) {
                withContext(Dispatchers.IO) {
                    Database.album(browseId).collect { album ->
                        if (album?.timestamp == null) {
                            YouTube.album(browseId)?.map { youtubeAlbum ->
                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = youtubeAlbum.title,
                                        thumbnailUrl = youtubeAlbum.thumbnail?.url,
                                        year = youtubeAlbum.year,
                                        authorsText = youtubeAlbum.authors?.joinToString("") { it.name },
                                        shareUrl = youtubeAlbum.url,
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    youtubeAlbum.items?.mapIndexedNotNull { position, albumItem ->
                                        albumItem.toMediaItem(browseId, youtubeAlbum)?.let { mediaItem ->
                                            Database.insert(mediaItem)
                                            SongAlbumMap(
                                                songId = mediaItem.mediaId,
                                                albumId = browseId,
                                                position = position
                                            )
                                        }
                                    } ?: emptyList()
                                )

                                null
                            }
                        } else {
                            value = Result.success(album)
                        }
                    }
                }
            }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChanged = {},
                tabColumnContent = { Item ->
                    Item(0, "Overview", R.drawable.sparkles)
                }
            ) {  currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    AlbumOverview(
                        albumResult = albumResult,
                        browseId = browseId,
                    )
                }
            }
        }
    }
}
