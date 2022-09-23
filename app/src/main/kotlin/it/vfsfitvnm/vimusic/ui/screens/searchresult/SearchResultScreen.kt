package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SmallAlbumItem
import it.vfsfitvnm.vimusic.ui.views.SmallArtistItem
import it.vfsfitvnm.vimusic.ui.views.SmallPlaylistItem
import it.vfsfitvnm.vimusic.ui.views.SmallSongItem
import it.vfsfitvnm.vimusic.ui.views.SmallVideoItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.searchResultScreenTabIndexKey
import it.vfsfitvnm.youtubemusic.YouTube

@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        playlistRoute { browseId ->
            PlaylistScreen(
                browseId = browseId ?: "browseId cannot be null"
            )
        }

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
                    0 -> YouTube.Item.Song.Filter
                    1 -> YouTube.Item.Album.Filter
                    2 -> YouTube.Item.Artist.Filter
                    3 -> YouTube.Item.Video.Filter
                    4 -> YouTube.Item.CommunityPlaylist.Filter
                    5 -> YouTube.Item.FeaturedPlaylist.Filter
                    else -> error("unreachable")
                }.value

                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailSizePx = Dimensions.thumbnails.song.px

                            ItemSearchResultTab<YouTube.Item.Song>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain
                            ) { song ->
                                SmallSongItem(
                                    song = song,
                                    thumbnailSizePx = thumbnailSizePx,
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(song.asMediaItem)
                                        binder?.setupRadio(song.info.endpoint)
                                    }
                                )
                            }
                        }

                        1 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResultTab<YouTube.Item.Album>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain
                            ) { album ->
                                SmallAlbumItem(
                                    album = album,
                                    thumbnailSizePx = thumbnailSizePx,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { albumRoute(album.info.endpoint?.browseId) }
                                        )
                                        .padding(
                                            vertical = Dimensions.itemsVerticalPadding,
                                            horizontal = 16.dp
                                        )
                                )
                            }
                        }

                        2 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResultTab<YouTube.Item.Artist>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                isArtists = true
                            ) { artist ->
                                SmallArtistItem(
                                    artist = artist,
                                    thumbnailSizePx = thumbnailSizePx,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { artistRoute(artist.info.endpoint?.browseId) }
                                        )
                                        .padding(
                                            vertical = Dimensions.itemsVerticalPadding,
                                            horizontal = 16.dp
                                        )
                                )
                            }
                        }
                        3 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailSizePx = Dimensions.thumbnails.song.px

                            ItemSearchResultTab<YouTube.Item.Video>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain
                            ) { video ->
                                SmallVideoItem(
                                    video = video,
                                    thumbnailSizePx = thumbnailSizePx,
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(video.asMediaItem)
                                        binder?.setupRadio(video.info.endpoint)
                                    }
                                )
                            }
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResultTab<YouTube.Item.Playlist>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain
                            ) { playlist ->
                                SmallPlaylistItem(
                                    playlist = playlist,
                                    thumbnailSizePx = thumbnailSizePx,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .clickable(
                                            indication = rememberRipple(bounded = true),
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = { playlistRoute(playlist.info.endpoint?.browseId) }
                                        )
                                        .padding(
                                            vertical = Dimensions.itemsVerticalPadding,
                                            horizontal = 16.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
