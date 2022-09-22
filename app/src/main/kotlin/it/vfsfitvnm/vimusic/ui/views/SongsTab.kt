package it.vfsfitvnm.vimusic.ui.views

import android.app.Application
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerAwarePaddingValues
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.mutableStatePreferenceOf
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.putEnum
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SongsTabViewModel(application: Application) : AndroidViewModel(application) {
    var items by mutableStateOf(emptyList<DetailedSong>())
        private set

    var sortBy by mutableStatePreferenceOf(preferences.getEnum(songSortByKey, SongSortBy.DateAdded)) {
        preferences.edit { putEnum(songSortByKey, it) }
        collectItems(sortBy = it)
    }

    var sortOrder by mutableStatePreferenceOf(preferences.getEnum(songSortOrderKey, SortOrder.Ascending)) {
        preferences.edit { putEnum(songSortOrderKey, it) }
        collectItems(sortOrder = it)
    }

    private var job: Job? = null

    private val preferences: SharedPreferences
        get() = getApplication<Application>().preferences

    init {
        collectItems()
    }

    private fun collectItems(sortBy: SongSortBy = this.sortBy, sortOrder: SortOrder = this.sortOrder) {
        job?.cancel()
        job = viewModelScope.launch {
            Database.songs(sortBy, sortOrder).flowOn(Dispatchers.IO).collect {
                items = it
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SongsTab(
    viewModel: SongsTabViewModel = viewModel()
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    val thumbnailSize = Dimensions.thumbnails.song.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (viewModel.sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(128.dp)
                    .fillMaxWidth()
            ) {
                BasicText(
                    text = "Songs",
                    style = typography.xxl.medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                ) {
                    @Composable
                    fun Item(
                        @DrawableRes iconId: Int,
                        sortBy: SongSortBy
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
                        iconId = R.drawable.trending,
                        sortBy = SongSortBy.PlayTime
                    )

                    Item(
                        iconId = R.drawable.text,
                        sortBy = SongSortBy.Title
                    )

                    Item(
                        iconId = R.drawable.calendar,
                        sortBy = SongSortBy.DateAdded
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
        }

        itemsIndexed(
            items = viewModel.items,
            key = { _, song -> song.id }
        ) { index, song ->
            SongItem(
                song = song,
                thumbnailSize = thumbnailSize,
                onClick = {
                    binder?.stopRadio()
                    binder?.player?.forcePlayAtIndex(
                        viewModel.items.map(DetailedSong::asMediaItem),
                        index
                    )
                },
                menuContent = {
                    InHistoryMediaItemMenu(song = song)
                },
                onThumbnailContent = {
                    AnimatedVisibility(
                        visible = viewModel.sortBy == SongSortBy.PlayTime,
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
