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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbumList(
    onAlbumClick: (Album) -> Unit,
    viewModel: HomeAlbumListViewModel = viewModel()
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    val thumbnailSizeDp = Dimensions.thumbnails.song * 2
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (viewModel.sortOrder == SortOrder.Ascending) 0f else 180f,
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
                    sortBy: AlbumSortBy
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

                Item(
                    iconId = R.drawable.calendar,
                    sortBy = AlbumSortBy.Year
                )

                Item(
                    iconId = R.drawable.text,
                    sortBy = AlbumSortBy.Title
                )

                Item(
                    iconId = R.drawable.time,
                    sortBy = AlbumSortBy.DateAdded
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

        items(
            items = viewModel.items,
            key = Album::id
        ) { album ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clickable(
                        indication = rippleIndication,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onAlbumClick(album) }
                    )
                    .padding(vertical = Dimensions.itemsVerticalPadding, horizontal = 16.dp)
                    .fillMaxWidth()
                    .animateItemPlacement()
            ) {
                AsyncImage(
                    model = album.thumbnailUrl?.thumbnail(thumbnailSizePx),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .size(thumbnailSizeDp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = album.title ?: "",
                        style = typography.xs.semiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = album.authorsText ?: "",
                        style = typography.xs.semiBold.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    album.year?.let { year ->
                        BasicText(
                            text = year,
                            style = typography.xxs.semiBold.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
