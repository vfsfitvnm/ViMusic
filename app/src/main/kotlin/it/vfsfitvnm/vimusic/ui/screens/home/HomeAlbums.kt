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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.savers.AlbumListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.albumSortByKey
import it.vfsfitvnm.vimusic.utils.albumSortOrderKey
import it.vfsfitvnm.vimusic.utils.produceSaveableState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbums(
    onAlbumClick: (Album) -> Unit
) {
    val (colorPalette) = LocalAppearance.current

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Descending)

    val items by produceSaveableState(
        initialValue = emptyList(),
        stateSaver = AlbumListSaver,
        sortBy, sortOrder,
    ) {
        Database
            .albums(sortBy, sortOrder)
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

    LazyColumn(
        contentPadding = LocalPlayerAwarePaddingValues.current,
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0
        ) {
            Header(title = "Albums") {
                @Composable
                fun Item(
                    @DrawableRes iconId: Int,
                    targetSortBy: AlbumSortBy
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
                    iconId = R.drawable.calendar,
                    targetSortBy = AlbumSortBy.Year
                )

                Item(
                    iconId = R.drawable.text,
                    targetSortBy = AlbumSortBy.Title
                )

                Item(
                    iconId = R.drawable.time,
                    targetSortBy = AlbumSortBy.DateAdded
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

        items(
            items = items,
            key = Album::id
        ) { album ->
            AlbumItem(
                album = album,
                thumbnailSizePx = thumbnailSizePx,
                thumbnailSizeDp = thumbnailSizeDp,
                modifier = Modifier
                    .clickable(
                        indication = rippleIndication,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onAlbumClick(album) }
                    )
                    .animateItemPlacement()
            )
        }
    }
}
