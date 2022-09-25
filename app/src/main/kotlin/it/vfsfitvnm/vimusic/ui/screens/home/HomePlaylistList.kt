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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.views.BuiltInPlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistPreviewItem
import it.vfsfitvnm.vimusic.utils.medium

@ExperimentalFoundationApi
@Composable
fun HomePlaylistList(
    viewModel: HomePlaylistListViewModel = viewModel(),
    onBuiltInPlaylistClicked: (BuiltInPlaylist) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current

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

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (viewModel.sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

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
        item(
            key = "header",
            contentType = 0,
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Header(title = "Playlists") {
                @Composable
                fun Item(
                    @DrawableRes iconId: Int,
                    sortBy: PlaylistSortBy
                ) {
                    Image(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (viewModel.sortBy == sortBy) colorPalette.text else colorPalette.textDisabled),
                        modifier = Modifier
                            .clickable { viewModel.sortBy = sortBy }
                            .padding(all = 4.dp)
                            .size(18.dp)
                    )
                }

                BasicText(
                    text = "New playlist",
                    style = typography.xxs.medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isCreatingANewPlaylist = true }
                        .background(colorPalette.background2)
                        .padding(all = 8.dp)
                        .padding(horizontal = 8.dp)
                )

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                Item(
                    iconId = R.drawable.medical,
                    sortBy = PlaylistSortBy.SongCount
                )

                Item(
                    iconId = R.drawable.text,
                    sortBy = PlaylistSortBy.Name
                )

                Item(
                    iconId = R.drawable.calendar,
                    sortBy = PlaylistSortBy.DateAdded
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
                        .clickable { viewModel.sortOrder = !viewModel.sortOrder }
                        .padding(all = 4.dp)
                        .size(18.dp)
                        .graphicsLayer { rotationZ = sortOrderIconRotation }
                )
            }
        }

        item(key = "favorites") {
            BuiltInPlaylistItem(
                icon = R.drawable.heart,
                colorTint = colorPalette.red,
                name = "Favorites",
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onBuiltInPlaylistClicked(BuiltInPlaylist.Favorites) }
                    )
            )
        }

        item(key = "offline") {
            BuiltInPlaylistItem(
                icon = R.drawable.airplane,
                colorTint = colorPalette.blue,
                name = "Offline",
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onBuiltInPlaylistClicked(BuiltInPlaylist.Offline) }
                    )
                    .animateItemPlacement()
            )
        }

        items(
            items = viewModel.items,
            key = { it.playlist.id }
        ) { playlistPreview ->
            PlaylistPreviewItem(
                playlistPreview = playlistPreview,
                modifier = Modifier
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onPlaylistClicked(playlistPreview.playlist) }
                    )
                    .animateItemPlacement()
            )
        }
    }
}
