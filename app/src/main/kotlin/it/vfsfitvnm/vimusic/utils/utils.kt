package it.vfsfitvnm.vimusic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongWithAuthors
import it.vfsfitvnm.vimusic.models.SongWithInfo
import it.vfsfitvnm.youtubemusic.YouTube

fun Context.shareAsYouTubeSong(mediaItem: MediaItem) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaItem.mediaId}")
    }

    startActivity(Intent.createChooser(sendIntent, null))
}

fun Database.insert(mediaItem: MediaItem): Song {
    return internal.runInTransaction<Song> {
        Database.song(mediaItem.mediaId)?.let {
            return@runInTransaction it
        }

        val albumInfo = mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(
                text = mediaItem.mediaMetadata.albumTitle!!.toString(),
                browseId = albumId
            )
        }

        val albumInfoId = albumInfo?.let { insert(it) }

        val authorsInfo =
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras!!.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.mapIndexed { index, artistName ->
                        Info(
                            text = artistName,
                            browseId = artistIds.getOrNull(index)
                        )
                    }
                }
            }

        val song = Song(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title!!.toString(),
            albumInfoId = albumInfoId,
            durationText = mediaItem.mediaMetadata.extras?.getString("durationText")!!,
            thumbnailUrl = mediaItem.mediaMetadata.artworkUri!!.toString()
        )

        insert(song)

        val authorsInfoId = authorsInfo?.let { insert(authorsInfo) }

        authorsInfoId?.forEach { authorInfoId ->
            insert(
                SongWithAuthors(
                    songId = mediaItem.mediaId,
                    authorInfoId = authorInfoId
                )
            )
        }

        return@runInTransaction song
    }
}

val YouTube.Item.Song.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(info.endpoint!!.videoId)
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
                        "artistNames" to authors.map { it.name },
                        "artistIds" to authors.map { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

val YouTube.Item.Video.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaId(info.endpoint!!.videoId)
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
                        "artistNames" to if (isOfficialMusicVideo) authors.map { it.name } else null,
                        "artistIds" to if (isOfficialMusicVideo) authors.map { it.endpoint?.browseId } else null,
                    )
                )
                .build()
        )
        .build()

val SongWithInfo.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(authors?.joinToString("") { it.text })
                .setAlbumTitle(album?.text)
                .setArtworkUri(song.thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "videoId" to song.id,
                        "albumId" to album?.browseId,
                        "artistNames" to authors?.map { it.text },
                        "artistIds" to authors?.map { it.browseId },
                        "durationText" to song.durationText,
                        "loudnessDb" to song.loudnessDb
                    )
                )
                .build()
        )
        .setMediaId(song.id)
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
                        "artistNames" to (authors ?: playlistOrAlbum.authors)?.map { it.name },
                        "artistIds" to (authors ?: playlistOrAlbum.authors)?.map { it.endpoint?.browseId }
                    )
                )
                .build()
        )
        .setMediaId(info.endpoint?.videoId ?: return null)
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