package it.vfsfitvnm.vimusic.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.compose.persist.PersistMapCleanup
import it.vfsfitvnm.compose.persist.persist
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.BrowseBody
import it.vfsfitvnm.innertube.models.bodies.ContinuationBody
import it.vfsfitvnm.innertube.requests.artistPage
import it.vfsfitvnm.innertube.requests.itemsPage
import it.vfsfitvnm.innertube.utils.from
import it.vfsfitvnm.compose.routing.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.components.themed.adaptiveThumbnailContent
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.items.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.searchresult.ItemsPage
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.artistScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun ArtistScreen(browseId: String) {
    val saveableStateHolder = rememberSaveableStateHolder()

    var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)

    PersistMapCleanup(tagPrefix = "artist/$browseId/")

    var artist by persist<Artist?>("artist/$browseId/artist")

    var artistPage by persist<Innertube.ArtistPage?>("artist/$browseId/artistPage")

    LaunchedEffect(Unit) {
        Database
            .artist(browseId)
            .combine(snapshotFlow { tabIndex }.map { it != 4 }) { artist, mustFetch -> artist to mustFetch }
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch)) {
                    withContext(Dispatchers.IO) {
                        Innertube.artistPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.name,
                                        thumbnailUrl = currentArtistPage.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt
                                    )
                                )
                            }
                    }
                }
            }
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val thumbnailContent =
                adaptiveThumbnailContent(
                    artist?.timestamp == null,
                    artist?.thumbnailUrl,
                    CircleShape
                )

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (artist?.timestamp == null) {
                        HeaderPlaceholder(
                            modifier = Modifier
                                .shimmer()
                        )
                    } else {
                        val (colorPalette) = LocalAppearance.current
                        val context = LocalContext.current

                        Header(title = artist?.name ?: "Unknown") {
                            textButton?.invoke()

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                            )

                            HeaderIconButton(
                                icon = if (artist?.bookmarkedAt == null) {
                                    R.drawable.bookmark_outline
                                } else {
                                    R.drawable.bookmark
                                },
                                color = colorPalette.accent,
                                onClick = {
                                    val bookmarkedAt =
                                        if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        artist
                                            ?.copy(bookmarkedAt = bookmarkedAt)
                                            ?.let(Database::update)
                                    }
                                }
                            )

                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://music.youtube.com/channel/$browseId"
                                        )
                                    }

                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                            )
                        }
                    }
                }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = { tabIndex = it },
                tabColumnContent = { Item ->
                    Item(0, "Overview", R.drawable.sparkles)
                    Item(1, "Songs", R.drawable.musical_notes)
                    Item(2, "Albums", R.drawable.disc)
                    Item(3, "Singles", R.drawable.disc)
                    Item(4, "Library", R.drawable.library)
                },
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> ArtistOverview(
                            youtubeArtistPage = artistPage,
                            thumbnailContent = thumbnailContent,
                            headerContent = headerContent,
                            onAlbumClick = { albumRoute(it) },
                            onViewAllSongsClick = { tabIndex = 1 },
                            onViewAllAlbumsClick = { tabIndex = 2 },
                            onViewAllSinglesClick = { tabIndex = 3 },
                        )

                        1 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "artist/$browseId/songs",
                                headerContent = headerContent,
                                itemsPageProvider = artistPage?.let {
                                    ({ continuation ->
                                        continuation?.let {
                                            Innertube.itemsPage(
                                                body = ContinuationBody(continuation = continuation),
                                                fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                            )
                                        } ?: artistPage
                                            ?.songsEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Innertube.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params,
                                                    ),
                                                    fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                                )
                                            }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.songs,
                                                continuation = null
                                            )
                                        )
                                    })
                                },
                                itemContent = { song ->
                                    SongItem(
                                        song = song,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        thumbnailSizePx = thumbnailSizePx,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onLongClick = {
                                                    menuState.display {
                                                        NonQueuedMediaItemMenu(
                                                            onDismiss = menuState::hide,
                                                            mediaItem = song.asMediaItem,
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlay(song.asMediaItem)
                                                    binder?.setupRadio(song.info?.endpoint)
                                                }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    SongItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "artist/$browseId/albums",
                                headerContent = headerContent,
                                emptyItemsText = "This artist didn't release any album",
                                itemsPageProvider = artistPage?.let {
                                    ({ continuation ->
                                        continuation?.let {
                                            Innertube.itemsPage(
                                                body = ContinuationBody(continuation = continuation),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                            )
                                        } ?: artistPage
                                            ?.albumsEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Innertube.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params,
                                                    ),
                                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                                )
                                            }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.albums,
                                                continuation = null
                                            )
                                        )
                                    })
                                },
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = { albumRoute(album.key) })
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        3 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "artist/$browseId/singles",
                                headerContent = headerContent,
                                emptyItemsText = "This artist didn't release any single",
                                itemsPageProvider = artistPage?.let {
                                    ({ continuation ->
                                        continuation?.let {
                                            Innertube.itemsPage(
                                                body = ContinuationBody(continuation = continuation),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                            )
                                        } ?: artistPage
                                            ?.singlesEndpoint
                                            ?.takeIf { it.browseId != null }
                                            ?.let { endpoint ->
                                                Innertube.itemsPage(
                                                    body = BrowseBody(
                                                        browseId = endpoint.browseId!!,
                                                        params = endpoint.params,
                                                    ),
                                                    fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                                )
                                            }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.singles,
                                                continuation = null
                                            )
                                        )
                                    })
                                },
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = { albumRoute(album.key) })
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        4 -> ArtistLocalSongs(
                            browseId = browseId,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent,
                        )
                    }
                }
            }
        }
    }
}
