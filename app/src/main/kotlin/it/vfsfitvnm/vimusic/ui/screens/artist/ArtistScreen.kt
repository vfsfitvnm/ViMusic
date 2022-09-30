package it.vfsfitvnm.vimusic.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.PartialArtist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.savers.ArtistSaver
import it.vfsfitvnm.vimusic.savers.YouTubeAlbumListSaver
import it.vfsfitvnm.vimusic.savers.YouTubeArtistPageSaver
import it.vfsfitvnm.vimusic.savers.YouTubeSongListSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.searchresult.SearchResult
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemShimmer
import it.vfsfitvnm.vimusic.ui.views.SmallSongItem
import it.vfsfitvnm.vimusic.ui.views.SmallSongItemShimmer
import it.vfsfitvnm.vimusic.utils.artistScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.produceSaveableLazyOneShotState
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.youtubemusic.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
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

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isError by remember {
        mutableStateOf(false)
    }

    val youtubeArtist by produceSaveableLazyOneShotState(
        initialValue = null,
        stateSaver = nullableSaver(YouTubeArtistPageSaver)
    ) {
        println("${System.currentTimeMillis()}, computing lazyEffect (youtubeArtistResult = ${value?.name})!")

        isLoading = true
        withContext(Dispatchers.IO) {
            YouTube.artist(browseId)?.onSuccess { youtubeArtist ->
                value = youtubeArtist

                query {
                    Database.upsert(
                        PartialArtist(
                            id = browseId,
                            name = youtubeArtist.name,
                            thumbnailUrl = youtubeArtist.thumbnail?.url,
                            info = youtubeArtist.description,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                isError = false
                isLoading = false
            }?.onFailure {
                println("error (1): $it")
                isError = true
                isLoading = false
            }
        }
    }

    val artist by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(ArtistSaver),
    ) {
        Database
            .artist(browseId)
            .flowOn(Dispatchers.IO)
            .filter {
                val hasToFetch = it?.timestamp == null
                if (hasToFetch) {
                    youtubeArtist?.name
                }
                !hasToFetch
            }
            .collect { value = it }
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val bookmarkIconContent: @Composable () -> Unit = {
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
                            val bookmarkedAt = if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                            query {
                                artist?.copy(bookmarkedAt = bookmarkedAt)?.let(Database::update)
                            }
                        }
                        .padding(all = 4.dp)
                        .size(18.dp)
                )
            }

            val shareIconContent: @Composable () -> Unit = {
                val context = LocalContext.current

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
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> ArtistOverview(
                            artist = artist,
                            youtubeArtist = youtubeArtist,
                            isLoading = isLoading,
                            isError = isError,
                            bookmarkIconContent = bookmarkIconContent,
                            shareIconContent = shareIconContent,
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
                                artist = artist,
                                youtubeArtist = youtubeArtist,
                                isLoading = isLoading,
                                isError = isError,
                                stateSaver = YouTubeSongListSaver,
                                bookmarkIconContent = bookmarkIconContent,
                                shareIconContent = shareIconContent,
                                itemsProvider = { continuation ->
                                    youtubeArtist
                                        ?.songsEndpoint
                                        ?.browseId
                                        ?.let { browseId ->
                                            YouTube.items(browseId, continuation, YouTube.Item.Song::from)?.map { result ->
                                                result?.continuation to result?.items
                                            }
                                        }
                                },
                                itemContent = { song ->
                                    SmallSongItem(
                                        song = song,
                                        thumbnailSizePx = thumbnailSizePx,
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info?.endpoint)
                                        }
                                    )
                                },
                                itemShimmer = {
                                    SmallSongItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                        2 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ArtistContent(
                                artist = artist,
                                youtubeArtist = youtubeArtist,
                                isLoading = isLoading,
                                isError = isError,
                                stateSaver = YouTubeAlbumListSaver,
                                bookmarkIconContent = bookmarkIconContent,
                                shareIconContent = shareIconContent,
                                itemsProvider = {
                                    youtubeArtist
                                        ?.albumsEndpoint
                                        ?.let { endpoint ->
                                            YouTube.items2(browseId, endpoint.params, YouTube.Item.Album::from)?.map { result ->
                                                result?.continuation to result?.items
                                            }
                                        }
                                },
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
                                itemShimmer = {
                                    AlbumItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                        3 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ArtistContent(
                                artist = artist,
                                youtubeArtist = youtubeArtist,
                                isLoading = isLoading,
                                isError = isError,
                                stateSaver = YouTubeAlbumListSaver,
                                bookmarkIconContent = bookmarkIconContent,
                                shareIconContent = shareIconContent,
                                itemsProvider = {
                                    youtubeArtist
                                        ?.singlesEndpoint
                                        ?.let { endpoint ->
                                            YouTube.items2(browseId, endpoint.params, YouTube.Item.Album::from)?.map { result ->
                                                result?.continuation to result?.items
                                            }
                                        }
                                },
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
                                itemShimmer = {
                                    AlbumItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                        4 -> ArtistLocalSongsList(
                            browseId = browseId,
                            artist = artist,
                            isLoading = isLoading,
                            isError = isError,
                            bookmarkIconContent = bookmarkIconContent,
                            shareIconContent = shareIconContent
                        )
                    }
                }
            }
        }
    }
}
