package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail

@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    mediaItem: MediaItem,
    thumbnailSize: Int,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SongItem(
        thumbnailModel = mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSize),
        title = mediaItem.mediaMetadata.title!!.toString(),
        authors = mediaItem.mediaMetadata.artist.toString(),
        durationText = mediaItem.mediaMetadata.extras?.getString("durationText") ?: "?",
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    song: DetailedSong,
    thumbnailSize: Int,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SongItem(
        thumbnailModel = song.thumbnailUrl?.thumbnail(thumbnailSize),
        title = song.title,
        authors = song.artistsText ?: "",
        durationText = song.durationText,
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    thumbnailModel: Any?,
    title: String,
    authors: String,
    durationText: String?,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SongItem(
        title = title,
        authors = authors,
        durationText = durationText,
        onClick = onClick,
        startContent = {
            Box(
                modifier = Modifier
                    .size(Dimensions.thumbnails.song)
            ) {
                AsyncImage(
                    model = thumbnailModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(ThumbnailRoundness.shape)
                        .fillMaxSize()
                )

                onThumbnailContent?.invoke(this)
            }
        },
        menuContent = menuContent,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun SongItem(
    title: String,
    authors: String?,
    durationText: String?,
    onClick: () -> Unit,
    startContent: @Composable () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val menuState = LocalMenuState.current
    val (_, typography) = LocalAppearance.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .combinedClickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onLongClick = {
                    menuState.display(menuContent)
                },
                onClick = onClick
            )
            .fillMaxWidth()
            .padding(vertical = Dimensions.itemsVerticalPadding)
            .padding(start = 16.dp, end = if (trailingContent == null) 16.dp else 8.dp)
    ) {
        startContent()

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            BasicText(
                text = title,
                style = typography.xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = authors ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        durationText?.let {
            BasicText(
                text = durationText,
                style = typography.xxs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        trailingContent?.invoke()
    }
}
