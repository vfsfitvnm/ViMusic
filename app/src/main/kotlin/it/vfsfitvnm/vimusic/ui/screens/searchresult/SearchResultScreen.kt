package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.savers.InnertubeAlbumItemListSaver
import it.vfsfitvnm.vimusic.savers.InnertubeArtistItemListSaver
import it.vfsfitvnm.vimusic.savers.InnertubePlaylistItemListSaver
import it.vfsfitvnm.vimusic.savers.InnertubeSongItemListSaver
import it.vfsfitvnm.vimusic.savers.InnertubeVideoItemListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.ArtistItem
import it.vfsfitvnm.vimusic.ui.views.ArtistItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.PlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.ui.views.SongItemPlaceholder
import it.vfsfitvnm.vimusic.ui.views.VideoItem
import it.vfsfitvnm.vimusic.ui.views.VideoItemPlaceholder
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.searchResultScreenTabIndexKey
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.utils.from

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanges,
                tabColumnContent = { Item ->
                    Item(0, "Songs", R.drawable.musical_notes)
                    Item(1, "Albums", R.drawable.disc)
                    Item(2, "Artists", R.drawable.person)
                    Item(3, "Videos", R.drawable.film)
                    Item(4, "Playlists", R.drawable.playlist)
                    Item(5, "Featured", R.drawable.playlist)
                }
            ) { tabIndex ->
                val searchFilter = when (tabIndex) {
                    0 -> Innertube.SearchFilter.Song
                    1 -> Innertube.SearchFilter.Album
                    2 -> Innertube.SearchFilter.Artist
                    3 -> Innertube.SearchFilter.Video
                    4 -> Innertube.SearchFilter.CommunityPlaylist
                    5 -> Innertube.SearchFilter.FeaturedPlaylist
                    else -> error("unreachable")
                }.value

                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            SearchResult(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                stateSaver = InnertubeSongItemListSaver,
                                fromMusicShelfRendererContent = Innertube.SongItem.Companion::from,
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

                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            SearchResult(
                                query = query,
                                filter = searchFilter,
                                stateSaver = InnertubeAlbumItemListSaver,
                                onSearchAgain = onSearchAgain,
                                fromMusicShelfRendererContent = Innertube.AlbumItem.Companion::from,
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

                        2 -> {
                            val thumbnailSizeDp = 64.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            SearchResult(
                                query = query,
                                filter = searchFilter,
                                stateSaver = InnertubeArtistItemListSaver,
                                onSearchAgain = onSearchAgain,
                                fromMusicShelfRendererContent = Innertube.ArtistItem.Companion::from,
                                itemContent = { artist ->
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { artistRoute(artist.info?.endpoint?.browseId) }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    ArtistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                        3 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailHeightDp = 72.dp
                            val thumbnailWidthDp = 128.dp

                            SearchResult(
                                query = query,
                                filter = searchFilter,
                                stateSaver = InnertubeVideoItemListSaver,
                                onSearchAgain = onSearchAgain,
                                fromMusicShelfRendererContent = Innertube.VideoItem.Companion::from,
                                itemContent = { video ->
                                    VideoItem(
                                        video = video,
                                        thumbnailWidthDp = thumbnailWidthDp,
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(video.asMediaItem)
                                            binder?.setupRadio(video.info?.endpoint)
                                        }
                                    )
                                },
                                itemPlaceholderContent = {
                                    VideoItemPlaceholder(
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        thumbnailWidthDp = thumbnailWidthDp
                                    )
                                }
                            )
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            SearchResult(
                                query = query,
                                filter = searchFilter,
                                stateSaver = InnertubePlaylistItemListSaver,
                                onSearchAgain = onSearchAgain,
                                fromMusicShelfRendererContent = Innertube.PlaylistItem.Companion::from,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { playlistRoute(playlist.info?.endpoint?.browseId) }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
