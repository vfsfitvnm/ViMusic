package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.savers.PlaylistPreviewListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.SecondaryTextButton
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.items.PlaylistItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@ExperimentalFoundationApi
@Composable
fun HomePlaylists(
    onBuiltInPlaylist: (BuiltInPlaylist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    val (colorPalette) = LocalAppearance.current

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            hintText = "Enter the playlist name",
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
    }

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)

    val items by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = PlaylistPreviewListSaver,
        sortBy, sortOrder,
    ) {
        Database
            .playlistPreviews(sortBy, sortOrder)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val thumbnailSizeDp = 108.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    LazyVerticalGrid(
        columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
        contentPadding = LocalPlayerAwarePaddingValues.current,
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
        horizontalArrangement = Arrangement.spacedBy(
            space = Dimensions.itemsVerticalPadding * 2,
            alignment = Alignment.CenterHorizontally
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(colorPalette.background0)
    ) {
        item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {
            Header(title = "Playlists") {
                @Composable
                fun Item(
                    @DrawableRes iconId: Int,
                    targetSortBy: PlaylistSortBy
                ) {
                    Image(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (sortBy == targetSortBy) colorPalette.text else colorPalette.textDisabled),
                        modifier = Modifier
                            .clickable { sortBy = targetSortBy }
                            .padding(all = 4.dp)
                            .size(18.dp)
                    )
                }

                SecondaryTextButton(
                    text = "New playlist",
                    onClick = { isCreatingANewPlaylist = true }
                )

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                Item(
                    iconId = R.drawable.medical,
                    targetSortBy = PlaylistSortBy.SongCount
                )

                Item(
                    iconId = R.drawable.text,
                    targetSortBy = PlaylistSortBy.Name
                )

                Item(
                    iconId = R.drawable.time,
                    targetSortBy = PlaylistSortBy.DateAdded
                )

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )

                Image(
                    painter = painterResource(R.drawable.arrow_up),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable { sortOrder = !sortOrder }
                        .padding(all = 4.dp)
                        .size(18.dp)
                        .graphicsLayer { rotationZ = sortOrderIconRotation }
                )
            }
        }

        item(key = "favorites") {
            PlaylistItem(
                icon = R.drawable.heart,
                colorTint = colorPalette.red,
                name = "Favorites",
                songCount = null,
                thumbnailSizeDp = thumbnailSizeDp,
                alternative = true,
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onBuiltInPlaylist(BuiltInPlaylist.Favorites) }
                    )
                    .animateItemPlacement()
            )
        }

        item(key = "offline") {
            PlaylistItem(
                icon = R.drawable.airplane,
                colorTint = colorPalette.blue,
                name = "Offline",
                songCount = null,
                thumbnailSizeDp = thumbnailSizeDp,
                alternative = true,
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onBuiltInPlaylist(BuiltInPlaylist.Offline) }
                    )
                    .animateItemPlacement()
            )
        }

        items(items = items, key = { it.playlist.id }) { playlistPreview ->
            PlaylistItem(
                playlist = playlistPreview,
                thumbnailSizeDp = thumbnailSizeDp,
                thumbnailSizePx = thumbnailSizePx,
                alternative = true,
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onPlaylistClick(playlistPreview.playlist) }
                    )
                    .animateItemPlacement()
            )
        }
    }
}
