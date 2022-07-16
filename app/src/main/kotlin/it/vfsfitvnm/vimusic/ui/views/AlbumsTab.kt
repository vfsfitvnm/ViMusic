package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSection
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSectionSpacer
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownTextItem
import it.vfsfitvnm.vimusic.ui.components.themed.DropdownMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged

@ExperimentalFoundationApi
@Composable
fun AlbumsTab(
    onAlbumClicked: (Album) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("albumSortBy", AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference("albumSortOrder", SortOrder.Descending)

    val albums by remember(sortBy, sortOrder) {
        Database.albums(sortBy, sortOrder).distinctUntilChanged()
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    val thumbnailSizePx = with(LocalDensity.current) { Dimensions.thumbnails.song.roundToPx() }

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
                            isSelected = sortBy == AlbumSortBy.Title,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = AlbumSortBy.Title
                            }
                        )

                        DropDownTextItem(
                            text = "YEAR",
                            isSelected = sortBy == AlbumSortBy.Year,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = AlbumSortBy.Year
                            }
                        )

                        DropDownTextItem(
                            text = "DATE ADDED",
                            isSelected = sortBy == AlbumSortBy.DateAdded,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = AlbumSortBy.DateAdded
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
            items(
                items = albums,
                key = Album::id
            ) { album ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onAlbumClicked(album) }
                        )
                        .animateItemPlacement()
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = album.thumbnailUrl?.thumbnail(thumbnailSizePx),
                        contentDescription = null,
                        error = {
                            Box {
                                Image(
                                    painter = painterResource(R.drawable.disc),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(ThumbnailRoundness.shape)
                            .size(Dimensions.thumbnails.song)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        BasicText(
                            text = album.title ?: "Unknown",
                            style = typography.xs.semiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        album.authorsText?.let {
                            BasicText(
                                text = album.authorsText,
                                style = typography.xs.semiBold.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    album.year?.let {
                        BasicText(
                            text = album.year,
                            style = typography.xxs.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}