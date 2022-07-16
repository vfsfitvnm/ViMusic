package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.isCachedPlaylistShownKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers

@ExperimentalFoundationApi
@Composable
fun PlaylistsTab(
    onBuiltInPlaylistClicked: (BuiltInPlaylist) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isCachedPlaylistShown by rememberPreference(isCachedPlaylistShownKey, false)
    var sortBy by rememberPreference("playlistSortBy", PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference("playlistSortOrder", SortOrder.Ascending)

    val playlistPreviews by remember(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

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

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorPalette.background,
                            Color.Transparent
                        )
                    )
                )
                .fillMaxWidth()
                .zIndex(1f)
                .padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable {
                        isCreatingANewPlaylist = true
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .size(20.dp)
            )

            Box {
                var isSortMenuDisplayed by remember {
                    mutableStateOf(false)
                }

                Image(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .clickable {
                            isSortMenuDisplayed = true
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .size(20.dp)
                )

                DropdownMenu(
                    isDisplayed = isSortMenuDisplayed,
                    onDismissRequest = {
                        isSortMenuDisplayed = false
                    }
                ) {
                    DropDownSection {
                        DropDownTextItem(
                            text = "NAME",
                            isSelected = sortBy == PlaylistSortBy.Name,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = PlaylistSortBy.Name
                            }
                        )

                        DropDownTextItem(
                            text = "DATE ADDED",
                            isSelected = sortBy == PlaylistSortBy.DateAdded,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = PlaylistSortBy.DateAdded
                            }
                        )

                        DropDownTextItem(
                            text = "SONG COUNT",
                            isSelected = sortBy == PlaylistSortBy.SongCount,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = PlaylistSortBy.SongCount
                            }
                        )
                    }

                    DropDownSectionSpacer()

                    DropDownSection {
                        DropDownTextItem(
                            text = when (sortOrder) {
                                SortOrder.Ascending -> "ASCENDING"
                                SortOrder.Descending -> "DESCENDING"
                            },
                            onClick = {
                                isSortMenuDisplayed = false
                                sortOrder = !sortOrder
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(top = 36.dp)) {
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

            if (isCachedPlaylistShown) {
                item(key = "cached") {
                    BuiltInPlaylistItem(
                        icon = R.drawable.download,
                        colorTint = colorPalette.blue,
                        name = "Cached",
                        modifier = Modifier
                            .clickable(
                                indication = rememberRipple(bounded = true),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onBuiltInPlaylistClicked(BuiltInPlaylist.Cached) }
                            )
                            .animateItemPlacement()
                    )
                }
            }
            items(
                items = playlistPreviews,
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
}