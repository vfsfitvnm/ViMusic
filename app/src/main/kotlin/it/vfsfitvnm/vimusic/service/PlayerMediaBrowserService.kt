package it.vfsfitvnm.vimusic.service

import android.media.MediaDescription as BrowserMediaDescription
import android.media.browse.MediaBrowser.MediaItem as BrowserMediaItem
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.ServiceConnection
import android.media.session.MediaSession
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.service.media.MediaBrowserService
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.Player
import androidx.media3.datasource.cache.Cache
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongWithContentLength
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PlayerMediaBrowserService : MediaBrowserService(), ServiceConnection {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lastSongs = emptyList<Song>()

    private var bound = false

    override fun onDestroy() {
        if (bound) {
            unbindService(this)
        }
        super.onDestroy()
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        if (service is PlayerService.Binder) {
            bound = true
            sessionToken = service.mediaSession.sessionToken
            service.mediaSession.setCallback(SessionCallback(service.player, service.cache))
        }
    }

    override fun onServiceDisconnected(name: ComponentName) = Unit

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (clientUid == Process.myUid()
            || clientUid == Process.SYSTEM_UID
            || clientPackageName == "com.google.android.projection.gearhead"
        ) {
            bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
            BrowserRoot(
                MediaId.root,
                bundleOf("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT" to 1)
            )
        } else {
            null
        }
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<BrowserMediaItem>>) {
        runBlocking(Dispatchers.IO) {
            result.sendResult(
                when (parentId) {
                    MediaId.root -> mutableListOf(
                        songsBrowserMediaItem,
                        playlistsBrowserMediaItem,
                        albumsBrowserMediaItem
                    )

                    MediaId.songs -> Database
                        .songsByPlayTimeDesc()
                        .first()
                        .take(30)
                        .also { lastSongs = it }
                        .map { it.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            if (isNotEmpty()) add(0, shuffleBrowserMediaItem)
                        }

                    MediaId.playlists -> Database
                        .playlistPreviewsByDateAddedDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()
                        .apply {
                            add(0, favoritesBrowserMediaItem)
                            add(1, offlineBrowserMediaItem)
                        }

                    MediaId.albums -> Database
                        .albumsByRowIdDesc()
                        .first()
                        .map { it.asBrowserMediaItem }
                        .toMutableList()

                    else -> mutableListOf()
                }
            )
        }
    }

    private fun uriFor(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(id))
        .appendPath(resources.getResourceTypeName(id))
        .appendPath(resources.getResourceEntryName(id))
        .build()

    private val shuffleBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.shuffle)
                .setTitle("Shuffle")
                .setIconUri(uriFor(R.drawable.shuffle))
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private val songsBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.songs)
                .setTitle("Songs")
                .setIconUri(uriFor(R.drawable.musical_notes))
                .build(),
            BrowserMediaItem.FLAG_BROWSABLE
        )


    private val playlistsBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.playlists)
                .setTitle("Playlists")
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            BrowserMediaItem.FLAG_BROWSABLE
        )

    private val albumsBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.albums)
                .setTitle("Albums")
                .setIconUri(uriFor(R.drawable.disc))
                .build(),
            BrowserMediaItem.FLAG_BROWSABLE
        )

    private val favoritesBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.favorites)
                .setTitle("Favorites")
                .setIconUri(uriFor(R.drawable.heart))
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private val offlineBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.offline)
                .setTitle("Offline")
                .setIconUri(uriFor(R.drawable.airplane))
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private val Song.asBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.forSong(id))
                .setTitle(title)
                .setSubtitle(artistsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private val PlaylistPreview.asBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.forPlaylist(playlist.id))
                .setTitle(playlist.name)
                .setSubtitle("$songCount songs")
                .setIconUri(uriFor(R.drawable.playlist))
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private val Album.asBrowserMediaItem
        inline get() = BrowserMediaItem(
            BrowserMediaDescription.Builder()
                .setMediaId(MediaId.forAlbum(id))
                .setTitle(title)
                .setSubtitle(authorsText)
                .setIconUri(thumbnailUrl?.toUri())
                .build(),
            BrowserMediaItem.FLAG_PLAYABLE
        )

    private inner class SessionCallback(private val player: Player, private val cache: Cache) :
        MediaSession.Callback() {
        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = player.forceSeekToPrevious()
        override fun onSkipToNext() = player.forceSeekToNext()
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onSkipToQueueItem(id: Long) = player.seekToDefaultPosition(id.toInt())

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val data = mediaId?.split('/') ?: return
            var index = 0

            coroutineScope.launch {
                val mediaItems = when (data.getOrNull(0)) {
                    MediaId.shuffle -> lastSongs

                    MediaId.songs ->  data
                        .getOrNull(1)
                        ?.let { songId ->
                            index = lastSongs.indexOfFirst { it.id == songId }
                            lastSongs
                        }

                    MediaId.favorites -> Database
                        .favorites()
                        .first()
                        .shuffled()

                    MediaId.offline -> Database
                        .songsWithContentLength()
                        .first()
                        .filter { song ->
                            song.contentLength?.let {
                                cache.isCached(song.song.id, 0, it)
                            } ?: false
                        }
                        .map(SongWithContentLength::song)
                        .shuffled()

                    MediaId.playlists -> data
                        .getOrNull(1)
                        ?.toLongOrNull()
                        ?.let(Database::playlistWithSongs)
                        ?.first()
                        ?.songs
                        ?.shuffled()

                    MediaId.albums -> data
                        .getOrNull(1)
                        ?.let(Database::albumSongs)
                        ?.first()

                    else -> emptyList()
                }?.map(Song::asMediaItem) ?: return@launch

                withContext(Dispatchers.Main) {
                    player.forcePlayAtIndex(mediaItems, index.coerceIn(0, mediaItems.size))
                }
            }
        }
    }

    private object MediaId {
        const val root = "root"
        const val songs = "songs"
        const val playlists = "playlists"
        const val albums = "albums"

        const val favorites = "favorites"
        const val offline = "offline"
        const val shuffle = "shuffle"

        fun forSong(id: String) = "songs/$id"
        fun forPlaylist(id: Long) = "playlists/$id"
        fun forAlbum(id: String) = "albums/$id"
    }
}
