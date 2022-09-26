package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.savers.DetailedSongListSaver
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.SongItem
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.produceSaveableListState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeSongList() {
    println("[${System.currentTimeMillis()}] HomeSongList")
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val thumbnailSize = Dimensions.thumbnails.song.px

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    val items by produceSaveableListState(
        flowProvider = { Database.songs(sortBy, sortOrder) },
        stateSaver = DetailedSongListSaver,
        key1 = sortBy,
        key2 = sortOrder
    )

//    var items by rememberSaveable(stateSaver = DetailedSongListSaver) {
//        mutableStateOf(emptyList())
//    }
//
//    var hasToRecollect by rememberSaveable(sortBy, sortOrder) {
//        println("hasToRecollect: $sortBy, $sortOrder")
//        mutableStateOf(true)
//    }
//
//    LaunchedEffect(sortBy, sortOrder) {
//        println("[${System.currentTimeMillis()}] LaunchedEffect, $hasToRecollect, $sortBy, $sortOrder")
//        Database.songs(sortBy, sortOrder)
//            .flowOn(Dispatchers.IO)
//            .drop(if (hasToRecollect) 0 else 1)
//            .collect {
//                hasToRecollect = false
//                println("[${System.currentTimeMillis()}] collecting... ")
//                items = it
//            }
//    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    LazyColumn(
        contentPadding = LocalPlayerAwarePaddingValues.current,
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {
//        println("[${System.currentTimeMillis()}] LazyColumn")

        item(
            key = "header",
            contentType = 0
        ) {
            Header(title = "Songs") {
                @Composable
                fun Item(
                    @DrawableRes iconId: Int,
                    targetSortBy: SongSortBy
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
                    iconId = R.drawable.trending,
                    targetSortBy = SongSortBy.PlayTime
                )

                Item(
                    iconId = R.drawable.text,
                    targetSortBy = SongSortBy.Title
                )

                Item(
                    iconId = R.drawable.time,
                    targetSortBy = SongSortBy.DateAdded
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

        itemsIndexed(
            items = items,
            key = { _, song -> song.id }
        ) { index, song ->
            SongItem(
                song = song,
                thumbnailSize = thumbnailSize,
                onClick = {
                    items.map(DetailedSong::asMediaItem)?.let { mediaItems ->
                        binder?.stopRadio()
                        binder?.player?.forcePlayAtIndex(mediaItems, index)
                    }
                },
                menuContent = {
                    InHistoryMediaItemMenu(song = song)
                },
                onThumbnailContent = {
                    AnimatedVisibility(
                        visible = sortBy == SongSortBy.PlayTime,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {
                        BasicText(
                            text = song.formattedTotalPlayTime,
                            style = typography.xxs.semiBold.center.color(
                                Color.White
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.75f)
                                        )
                                    ),
                                    shape = ThumbnailRoundness.shape
                                )
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                        )
                    }
                },
                modifier = Modifier
                    .animateItemPlacement()
            )
        }
    }
}
