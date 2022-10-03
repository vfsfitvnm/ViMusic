package it.vfsfitvnm.vimusic.ui.screens.album

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.SongAlbumMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.savers.AlbumSaver
import it.vfsfitvnm.vimusic.savers.InnertubeAlbumsPageSaver
import it.vfsfitvnm.vimusic.savers.InnertubePlaylistOrAlbumPageSaver
import it.vfsfitvnm.vimusic.savers.nullableSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.HeaderPlaceholder
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.searchresult.ItemsPage
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemPlaceholder
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.youtubemusic.Innertube
import it.vfsfitvnm.youtubemusic.models.bodies.BrowseBody
import it.vfsfitvnm.youtubemusic.requests.albumPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun AlbumScreen(browseId: String) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabChanged) = rememberSaveable {
        mutableStateOf(0)
    }

    val album by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(AlbumSaver),
    ) {
        Database
            .album(browseId)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val innertubeAlbum by produceSaveableState(
        initialValue = null,
        stateSaver = nullableSaver(InnertubePlaylistOrAlbumPageSaver),
        tabIndex > 0
    ) {
        if (value != null || (tabIndex == 0 && withContext(Dispatchers.IO) { Database.albumTimestamp(browseId) } != null)) return@produceSaveableState

        withContext(Dispatchers.IO) {
            Innertube.albumPage(BrowseBody(browseId = browseId))
        }?.onSuccess { albumPage ->
            value = albumPage

            query {
                Database.upsert(
                    Album(
                        id = browseId,
                        title = albumPage.title,
                        thumbnailUrl = albumPage.thumbnail?.url,
                        year = albumPage.year,
                        authorsText = albumPage.authors?.joinToString("") { it.name ?: "" },
                        shareUrl = albumPage.url,
                        timestamp = System.currentTimeMillis()
                    ),
                    albumPage
                        .songsPage
                        ?.items
                        ?.map(Innertube.SongItem::asMediaItem)
                        ?.onEach(Database::insert)
                        ?.mapIndexed { position, mediaItem ->
                            SongAlbumMap(
                                songId = mediaItem.mediaId,
                                albumId = browseId,
                                position = position
                            )
                        } ?: emptyList()
                )
            }
        }
    }

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = { textButton ->
                if (album?.timestamp == null) {
                    HeaderPlaceholder(
                        modifier = Modifier
                            .shimmer()
                    )
                } else {
                    val (colorPalette) = LocalAppearance.current
                    val context = LocalContext.current

                    Header(title = album?.title ?: "Unknown") {
                        textButton?.invoke()

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        Image(
                            painter = painterResource(
                                if (album?.bookmarkedAt == null) {
                                    R.drawable.bookmark_outline
                                } else {
                                    R.drawable.bookmark
                                }
                            ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.accent),
                            modifier = Modifier
                                .clickable {
                                    val bookmarkedAt =
                                        if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        album
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
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .clickable {
                                    album?.shareUrl?.let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }
                                }
                                .padding(all = 4.dp)
                                .size(18.dp)
                        )
                    }
                }
            }

            val thumbnailContent: @Composable ColumnScope.() -> Unit = {
                val (colorPalette, _, thumbnailShape) = LocalAppearance.current

                if (album?.timestamp == null) {
                    Spacer(
                        modifier = Modifier
                            .shimmer()
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 16.dp)
                            .clip(CircleShape)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(colorPalette.shimmer)
                    )
                } else {
                    BoxWithConstraints(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        val thumbnailSizeDp = maxWidth - Dimensions.navigationRailWidth
                        val thumbnailSizePx = (thumbnailSizeDp - 32.dp).px

                        AsyncImage(
                            model = album?.thumbnailUrl?.thumbnail(thumbnailSizePx),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(all = 16.dp)
                                .clip(thumbnailShape)
                                .size(thumbnailSizeDp)
                        )
                    }
                }
            }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Songs", R.drawable.musical_notes)
                    Item(1, "Other versions", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> AlbumSongs(
                            browseId = browseId,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent,
                        )
                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                stateSaver = InnertubeAlbumsPageSaver,
                                headerContent = headerContent,
                                itemsPageProvider = innertubeAlbum?.let {
                                    ({
                                        Result.success(
                                            Innertube.ItemsPage(
                                                items = innertubeAlbum?.otherVersions,
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
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { albumRoute(album.key) }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
