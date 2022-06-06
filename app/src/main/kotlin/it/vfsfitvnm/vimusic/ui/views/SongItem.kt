package it.vfsfitvnm.vimusic.ui.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
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
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ThumbnailRoundness
import it.vfsfitvnm.vimusic.models.SongWithInfo
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold


@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    mediaItem: MediaItem,
    thumbnailSize: Int,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    SongItem(
        thumbnailModel = ImageRequest.Builder(LocalContext.current)
            .diskCacheKey(mediaItem.mediaId)
            .data("${mediaItem.mediaMetadata.artworkUri}-w$thumbnailSize-h$thumbnailSize")
            .build(),
        title = mediaItem.mediaMetadata.title!!.toString(),
        authors = mediaItem.mediaMetadata.artist.toString(),
        durationText = mediaItem.mediaMetadata.extras?.getString("durationText") ?: "?",
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        backgroundColor = backgroundColor,
        modifier = modifier,
    )
}

@ExperimentalAnimationApi
@Composable
@NonRestartableComposable
fun SongItem(
    song: SongWithInfo,
    thumbnailSize: Int,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    SongItem(
        thumbnailModel = "${song.song.thumbnailUrl}-w$thumbnailSize-h$thumbnailSize",
        title = song.song.title,
        authors = song.authors?.joinToString("") { it.text } ?: "",
        durationText = song.song.durationText,
        menuContent = menuContent,
        onClick = onClick,
        onThumbnailContent = onThumbnailContent,
        backgroundColor = backgroundColor,
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
    durationText: String,
    onClick: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    SongItem(
        title = title,
        authors = authors,
        durationText = durationText,
        onClick = onClick,
        startContent = {
            Box(
                modifier = Modifier
                    .size(54.dp)
            ) {
                AsyncImage(
                    model = thumbnailModel,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .clip(ThumbnailRoundness.shape)
                        .fillMaxSize()
                )

                onThumbnailContent?.invoke(this)
            }
        },
        menuContent = menuContent,
        backgroundColor = backgroundColor,
        modifier = modifier,
    )
}

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
    backgroundColor: Color? = null,
) {
    val menuState = LocalMenuState.current
    val colorPalette = LocalColorPalette.current
    val typography = LocalTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clickable(
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor ?: colorPalette.background)
            .padding(start = 16.dp, end = 8.dp)
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
                text = buildString {
                    append(authors)
                    if (authors?.isNotEmpty() == true && durationText != null) {
                        append(" • ")
                    }
                    append(durationText)
                },
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Image(
            painter = painterResource(R.drawable.ellipsis_vertical),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.textSecondary),
            modifier = Modifier
                .clickable {
                    menuState.display(menuContent)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .size(20.dp)
        )
    }
}