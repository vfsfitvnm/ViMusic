package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSection
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownSectionSpacer
import it.vfsfitvnm.vimusic.ui.components.themed.DropDownTextItem
import it.vfsfitvnm.vimusic.ui.components.themed.DropdownMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged

@ExperimentalFoundationApi
@Composable
fun ArtistsTab(
    onArtistClicked: (Artist) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("artistSortBy", ArtistSortBy.Name)
    var sortOrder by rememberPreference("artistSortOrder", SortOrder.Ascending)

    val artists by remember(sortBy, sortOrder) {
        Database.artists(sortBy, sortOrder).distinctUntilChanged()
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
                            isSelected = sortBy == ArtistSortBy.Name,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = ArtistSortBy.Name
                            }
                        )

                        DropDownTextItem(
                            text = "DATE ADDED",
                            isSelected = sortBy == ArtistSortBy.DateAdded,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = ArtistSortBy.DateAdded
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
                items = artists,
                key = Artist::id
            ) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clickable(
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onArtistClicked(artist) }
                        )
                        .animateItemPlacement()
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = artist.thumbnailUrl?.thumbnail(thumbnailSizePx),
                        contentDescription = null,
                        error = {
                            Box {
                                Image(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(24.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(Dimensions.thumbnails.song)
                    )

                    BasicText(
                        text = artist.name,
                        style = typography.xs.semiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }
    }
}