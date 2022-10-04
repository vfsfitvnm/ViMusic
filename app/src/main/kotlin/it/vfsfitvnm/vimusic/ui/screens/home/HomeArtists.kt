package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.remember
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
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.savers.ArtistListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.items.ArtistItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.artistSortByKey
import it.vfsfitvnm.vimusic.utils.artistSortOrderKey
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeArtistList(
    onArtistClick: (Artist) -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    var sortBy by rememberPreference(artistSortByKey, ArtistSortBy.DateAdded)
    var sortOrder by rememberPreference(artistSortOrderKey, SortOrder.Descending)

    val items by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = ArtistListSaver,
        sortBy, sortOrder,
    ) {
        Database
            .artists(sortBy, sortOrder)
            .flowOn(Dispatchers.IO)
            .collect { value = it }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song * 2
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val rippleIndication = rememberRipple(bounded = true)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
        contentPadding = LocalPlayerAwarePaddingValues.current,
        verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
        horizontalArrangement = Arrangement.spacedBy(
            space = Dimensions.itemsVerticalPadding * 2,
            alignment = Alignment.CenterHorizontally
        ),
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0,
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Header(title = "Artists") {
                @Composable
                fun Item(
                    @DrawableRes iconId: Int,
                    targetSortBy: ArtistSortBy
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

                Item(
                    iconId = R.drawable.text,
                    targetSortBy = ArtistSortBy.Name
                )

                Item(
                    iconId = R.drawable.time,
                    targetSortBy = ArtistSortBy.DateAdded
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

        items(items = items, key = Artist::id) { artist ->
            ArtistItem(
                artist = artist,
                thumbnailSizePx = thumbnailSizePx,
                thumbnailSizeDp = thumbnailSizeDp,
                alternative = true,
                modifier = Modifier
                    .clickable(
                        indication = rippleIndication,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onArtistClick(artist) }
                    )
//                    .requiredWidth(thumbnailSizeDp)
                    .animateItemPlacement()
            )
        }
    }
}
