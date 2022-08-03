package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import it.vfsfitvnm.vimusic.models.DetailedSong
import it.vfsfitvnm.youtubemusic.YouTube

fun Context.shareAsYouTubeSong(mediaItem: MediaItem) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaItem.mediaId}")
    }

    startActivity(Intent.createChooser(sendIntent, null))
}

val YouTube.Item.Song.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(info.endpoint!!.videoId!!)
        .setUri(info.endpoint!!.videoId)
        .setCustomCacheKey(info.endpoint!!.videoId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info.name)
                .setArtist(authors.joinToString("") { it.name })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "videoId" to info.endpoint!!.videoId,
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors.filter { it.endpoint != null }.map { it.name },
                        "artistIds" to authors.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

val YouTube.Item.Video.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(info.endpoint!!.videoId!!)
        .setUri(info.endpoint!!.videoId)
        .setCustomCacheKey(info.endpoint!!.videoId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info.name)
                .setArtist(authors.joinToString("") { it.name })
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "videoId" to info.endpoint!!.videoId,
                        "durationText" to durationText,
                        "artistNames" to if (isOfficialMusicVideo) authors.filter { it.endpoint != null }.map { it.name } else null,
                        "artistIds" to if (isOfficialMusicVideo) authors.mapNotNull { it.endpoint?.browseId } else null,
                    )
                )
                .build()
        )
        .build()

val DetailedSong.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistsText)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "videoId" to id,
                        "albumId" to albumId,
                        "artistNames" to artists?.map { it.name },
                        "artistIds" to artists?.map { it.id },
                        "durationText" to durationText
                    )
                )
                .build()
        )
        .setMediaId(id)
        .setUri(id)
        .setCustomCacheKey(id)
        .build()

fun YouTube.PlaylistOrAlbum.Item.toMediaItem(
    albumId: String,
    playlistOrAlbum: YouTube.PlaylistOrAlbum
): MediaItem? {
    val isFromAlbum = thumbnail == null

    return MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info.name)
                .setArtist((authors ?: playlistOrAlbum.authors)?.joinToString("") { it.name })
                .setAlbumTitle(if (isFromAlbum) playlistOrAlbum.title else album?.name)
                .setArtworkUri(if (isFromAlbum) playlistOrAlbum.thumbnail?.url?.toUri() else thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "videoId" to info.endpoint?.videoId,
                        "playlistId" to info.endpoint?.playlistId,
                        "albumId" to (if (isFromAlbum) albumId else album?.endpoint?.browseId),
                        "durationText" to durationText,
                        "artistNames" to (authors ?: playlistOrAlbum.authors)?.filter { it.endpoint != null }?.map { it.name },
                        "artistIds" to (authors ?: playlistOrAlbum.authors)?.mapNotNull { it.endpoint?.browseId }
                    )
                )
                .build()
        )
        .setMediaId(info.endpoint?.videoId ?: return null)
        .setUri(info.endpoint?.videoId ?: return null)
        .setCustomCacheKey(info.endpoint?.videoId ?: return null)
        .build()
}

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-s$size"
        else -> this
    }
}

fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}
