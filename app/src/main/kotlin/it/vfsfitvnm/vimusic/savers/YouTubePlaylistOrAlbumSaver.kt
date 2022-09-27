package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.youtubemusic.YouTube

object YouTubePlaylistOrAlbumSaver : Saver<YouTube.PlaylistOrAlbum, List<Any?>> {
    override fun SaverScope.save(value: YouTube.PlaylistOrAlbum): List<Any?> = listOf(
        value.title,
        value.authors?.let { with(YouTubeBrowseInfoListSaver) { save(it) } } ,
        value.year,
        value.thumbnail?.let { with(YouTubeThumbnailSaver) { save(it) } } ,
        value.songs?.let { with(YouTubeSongListSaver) { save(it) } },
        value.url
    )

    @Suppress("UNCHECKED_CAST")
    override fun restore(value: List<Any?>) = YouTube.PlaylistOrAlbum(
        title = value[0] as String?,
        authors = (value[1] as List<List<Any?>>?)?.let(YouTubeBrowseInfoListSaver::restore),
        year = value[2] as String?,
        thumbnail = (value[3] as List<Any?>?)?.let(YouTubeThumbnailSaver::restore),
        songs = (value[4] as List<List<Any?>>?)?.let(YouTubeSongListSaver::restore),
        url = value[5] as String?,
        continuation = null
    )
}
