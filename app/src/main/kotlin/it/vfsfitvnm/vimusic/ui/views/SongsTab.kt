package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.ui.components.themed.*
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.Dispatchers

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SongsTab() {
    val (colorPalette, typography) = LocalAppearance.current

    var sortBy by rememberPreference("songSortBy", SongSortBy.DateAdded)
    var sortOrder by rememberPreference("songSortOrder", SortOrder.Ascending)

    val songs by remember(sortBy, sortOrder) {
        Database.songs(sortBy, sortOrder)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    Box {
        val binder = LocalPlayerServiceBinder.current
        val thumbnailSize = Dimensions.thumbnails.song.px

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
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .clickable(enabled = songs.isNotEmpty()) {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs
                                .shuffled()
                                .map(DetailedSong::asMediaItem)
                        )
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
                            text = "PLAY TIME",
                            isSelected = sortBy == SongSortBy.PlayTime,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = SongSortBy.PlayTime
                            }
                        )

                        DropDownTextItem(
                            text = "TITLE",
                            isSelected = sortBy == SongSortBy.Title,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = SongSortBy.Title
                            }
                        )

                        DropDownTextItem(
                            text = "DATE ADDED",
                            isSelected = sortBy == SongSortBy.DateAdded,
                            onClick = {
                                isSortMenuDisplayed = false
                                sortBy = SongSortBy.DateAdded
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
            itemsIndexed(
                items = songs,
                key = { _, song -> song.song.id }
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSize = thumbnailSize,
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayAtIndex(
                            songs.map(DetailedSong::asMediaItem),
                            index
                        )
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
                                text = song.song.formattedTotalPlayTime,
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
}