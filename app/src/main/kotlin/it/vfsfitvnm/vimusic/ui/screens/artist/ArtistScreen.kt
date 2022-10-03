package it.vfsfitvnm.vimusic.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.PartialArtist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.savers.ArtistSaver
import it.vfsfitvnm.vimusic.savers.InnertubeAlbumsPageSaver
import it.vfsfitvnm.vimusic.savers.InnertubeArtistPageSaver
import it.vfsfitvnm.vimusic.savers.InnertubeSongsPageSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.searchresult.ArtistContent
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.ui.views.SongItemPlaceholder
import it.vfsfitvnm.vimusic.utils.artistScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.bodies.BrowseBody
import it.vfsfitvnm.youtubemusic.models.bodies.ContinuationBody
import it.vfsfitvnm.youtubemusic.requests.artistPage
import it.vfsfitvnm.youtubemusic.requests.itemsPage
import it.vfsfitvnm.youtubemusic.utils.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
fun ArtistScreen(browseId: String) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabIndexChanged) = rememberPreference(
        artistScreenTabIndexKey,
        defaultValue = 0
    )

    val artist by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(ArtistSaver),
    ) {
        Database
            .artist(browseId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val youtubeArtist by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(InnertubeArtistPageSaver),
        tabIndex < 4
    ) {
        if (value != null || (tabIndex == 4 && withContext(Dispatchers.IO) { Database.artistTimestamp(browseId) } != null)) return@produceSaveableState

        withContext(Dispatchers.IO) {
            Innertube.artistPage(browseId)
        }?.onSuccess { artistPage ->
            value = artistPage

            query {
                Database.upsert(
                    PartialArtist(
                        id = browseId,
                        name = artistPage.name,
                        thumbnailUrl = artistPage.thumbnail?.url,
                        info = artistPage.description,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val thumbnailContent: @Composable ColumnScope.() -> Unit = {
                if (artist?.timestamp == null) {
                    Spacer(
                        modifier = Modifier
                            .shimmer()
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 16.dp)
                            .clip(CircleShape)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(LocalAppearance.current.colorPalette.shimmer)
                    )
                } else {
                    BoxWithConstraints(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        val thumbnailSizeDp = maxWidth - Dimensions.navigationRailWidth
                        val thumbnailSizePx = (thumbnailSizeDp - 32.dp).px

                        AsyncImage(
                            model = artist?.thumbnailUrl?.thumbnail(thumbnailSizePx),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(all = 16.dp)
                                .clip(CircleShape)
                                .size(thumbnailSizeDp)
                        )
                    }
                }
            }

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = { textButton ->
                if (artist?.timestamp == null) {
                    HeaderPlaceholder(
                        modifier = Modifier
                            .shimmer()
                    )
                } else {
                    val context = LocalContext.current

                    Header(title = artist?.name ?: "Unknown") {
                        textButton?.invoke()

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        Image(
                            painter = painterResource(
                                if (artist?.bookmarkedAt == null) {
                                    R.drawable.bookmark_outline
                                } else {
                                    R.drawable.bookmark
                                }
                            ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalAppearance.current.colorPalette.accent),
                            modifier = Modifier
                                .clickable {
                                    val bookmarkedAt =
                                        if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        artist
                                            ?.copy(bookmarkedAt = bookmarkedAt)
                                            ?.let(Database::update)
                                    }
                                }
                                .padding(all = 4.dp)
                                .size(18.dp)
                        )

                        Image(
                            painter = painterResource(R.drawable.share_social),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalAppearance.current.colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://music.youtube.com/channel/$browseId"
                                        )
                                    }

                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            null
                                        )
                                    )
                                }
                                .padding(all = 4.dp)
                                .size(18.dp)
                        )
                    }
                }
            }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanged,
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
                            youtubeArtistPage = youtubeArtist,
                            thumbnailContent = thumbnailContent,
                            headerContent = headerContent,
                            onAlbumClick = { albumRoute(it) },
                            onViewAllSongsClick = { onTabIndexChanged(1) },
                            onViewAllAlbumsClick = { onTabIndexChanged(2) },
                            onViewAllSinglesClick = { onTabIndexChanged(3) },
                        )

                        1 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ArtistContent(
                                stateSaver = InnertubeSongsPageSaver,
                                headerContent = headerContent,
                                itemsPageProvider = youtubeArtist?.let {({ continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicResponsiveListItemRenderer = Innertube.SongItem::from,
                                        )
                                    } ?: youtubeArtist
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
                                            items = youtubeArtist?.songs,
                                            continuation = null
                                        )
                                    )
                                })},
                                itemContent = { song ->
                                    SongItem(
                                        song = song,
                                        thumbnailSizePx = thumbnailSizePx,
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info?.endpoint)
                                        }
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

                            ArtistContent(
                                stateSaver = InnertubeAlbumsPageSaver,
                                headerContent = headerContent,
                                itemsPageProvider = youtubeArtist?.let {({ continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                        )
                                    } ?: youtubeArtist
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
                                            items = youtubeArtist?.albums,
                                            continuation = null
                                        )
                                    )
                                })},
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { albumRoute(album.info?.endpoint?.browseId) }
                                            )
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

                            ArtistContent(
                                stateSaver = InnertubeAlbumsPageSaver,
                                headerContent = headerContent,
                                itemsPageProvider = youtubeArtist?.let {({ continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from,
                                        )
                                    } ?: youtubeArtist
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
                                            items = youtubeArtist?.singles,
                                            continuation = null
                                        )
                                    )
                                })},
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { albumRoute(album.info?.endpoint?.browseId) }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        4 -> ArtistLocalSongsList(
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
