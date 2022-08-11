package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.RevealDirection
import it.vfsfitvnm.vimusic.ui.styling.RevealSwipe
import it.vfsfitvnm.vimusic.utils.*

@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    mediaItem: MediaItem,
    swipeShow: Boolean,
    thumbnailSize: Int,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SongItem(
        thumbnailModel = ImageRequest.Builder(LocalContext.current)
            .diskCacheKey(mediaItem.mediaId)
            .data(mediaItem.mediaMetadata.artworkUri.thumbnail(thumbnailSize))
            .build(),
        title = mediaItem.mediaMetadata.title!!.toString(),
        authors = mediaItem.mediaMetadata.artist.toString(),
        durationText = mediaItem.mediaMetadata.extras?.getString("durationText") ?: "?",
        mediaItem = mediaItem,
        swipeShow = swipeShow,
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        backgroundColor = backgroundColor,
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
    swipeShow: Boolean,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {

    SongItem(
        thumbnailModel = song.thumbnailUrl?.thumbnail(thumbnailSize),
        title = song.title,
        authors = song.artistsText ?: "",
        durationText = song.durationText,
        mediaItem = song.asMediaItem,
        swipeShow = swipeShow,
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        backgroundColor = backgroundColor,
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
    swipeShow: Boolean,
    mediaItem: MediaItem,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {

    SongItem(
        title = title,
        authors = authors,
        durationText = durationText,
        onClick = onClick,
        mediaItem = mediaItem,
        swipeShow = swipeShow,
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
        backgroundColor = backgroundColor,
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@ExperimentalAnimationApi
@Composable
fun SongItem(
    title: String,
    authors: String?,
    durationText: String?,
    mediaItem: MediaItem,
    swipeShow: Boolean,
    onClick: () -> Unit,
    startContent: @Composable () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val menuState = LocalMenuState.current
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    if (swipeShow) {

        RevealSwipe(
            directions = setOf(
                RevealDirection.StartToEnd,
                RevealDirection.EndToStart
            ),
            enableDismiss = swipeShow,
            onBackgroundEndClick = { menuState.display(menuContent) },
            onBackgroundStartClick = { binder.player?.addNext(mediaItem) },
            hiddenContentStart = {
                Image(
                    painter = painterResource(R.drawable.play_skip_forward),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xffe1e1e2)),
                    modifier = Modifier
                        .padding(24.dp)
                        .size(24.dp)
                )
            },
            hiddenContentEnd = {
                Image(
                    painter = painterResource(R.drawable.ellipsis_horizontal),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xffe1e1e2)),
                    modifier = Modifier
                        .padding(24.dp)
                        .size(24.dp)
                )
            }) {
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
    } else {
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
}
