package it.vfsfitvnm.vimusic.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaDescription
import android.media.browse.MediaBrowser.MediaItem
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.service.media.MediaBrowserService
import it.vfsfitvnm.vimusic.BuildConfig
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.utils.MediaIDHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PlayerMediaBrowserService : MediaBrowserService() {

    var playerServiceBinder: PlayerService.Binder? = null
    var isBound = false

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, playerConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        if (!isCallerAllowed(clientPackageName, clientUid)) {
            return null
        }
        val extras = Bundle()
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE)
        return BrowserRoot(MEDIA_ROOT_ID, extras)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> result.sendResult(createMenuMediaItem())
            MEDIA_PLAYLISTS_ID -> result.sendResult(createPlaylistsMediaItem())
            MEDIA_FAVORITES_ID -> result.sendResult(createFavoritesMediaItem())
            MEDIA_SONGS_ID -> result.sendResult(createSongsMediaItem())
        }
    }

    private fun createFavoritesMediaItem(): MutableList<MediaItem> {
        val favorites = runBlocking(Dispatchers.IO) {
            Database.favorites().first()
        }.map { entry ->
            MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MediaIDHelper.createMediaIdForSong(entry.id))
                    .setTitle(entry.title)
                    .setSubtitle(entry.artistsText)
                    .setIconUri(
                        Uri.parse(entry.thumbnailUrl)
                    )
                    .build(), MediaItem.FLAG_PLAYABLE
            )
        }.toCollection(mutableListOf())
        if (favorites.isNotEmpty()) {
            favorites.add(
                0, MediaItem(
                    MediaDescription.Builder()
                        .setMediaId(MediaIDHelper.createMediaIdForRandomFavorites())
                        .setTitle("Play all random")
                        .setIconUri(
                            Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/shuffle")
                        )
                        .build(), MediaItem.FLAG_PLAYABLE
                )
            )
        }
        return favorites
    }

    private fun createSongsMediaItem(): MutableList<MediaItem> {
        val songs = runBlocking(Dispatchers.IO) {
            Database.songs(SongSortBy.DateAdded, SortOrder.Descending).first()
        }.map { entry ->
            MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MediaIDHelper.createMediaIdForSong(entry.id))
                    .setTitle(entry.title)
                    .setSubtitle(entry.artistsText)
                    .setIconUri(
                        Uri.parse(entry.thumbnailUrl)
                    )
                    .build(), MediaItem.FLAG_PLAYABLE
            )
        }.toCollection(mutableListOf())
        if (songs.isNotEmpty()) {
            songs.add(
                0, MediaItem(
                    MediaDescription.Builder()
                        .setMediaId(MediaIDHelper.createMediaIdForRandomSongs())
                        .setTitle("Play all random")
                        .setIconUri(
                            Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/shuffle")
                        )
                        .build(), MediaItem.FLAG_PLAYABLE
                )
            )
        }
        return songs
    }

    private fun createPlaylistsMediaItem(): MutableList<MediaItem> {
        return runBlocking(Dispatchers.IO) {
            Database.playlistPreviews(PlaylistSortBy.DateAdded, SortOrder.Descending).first()
        }.map { entry ->
            MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MediaIDHelper.createMediaIdForPlaylist(entry.playlist.id))
                    .setTitle(entry.playlist.name)
                    .setSubtitle("${entry.songCount} songs")
                    .setIconUri(
                        Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/playlist")
                    )
                    .build(), MediaItem.FLAG_PLAYABLE
            )
        }.toCollection(mutableListOf())
    }

    private fun createMenuMediaItem(): MutableList<MediaItem> {
        return mutableListOf(
            MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MEDIA_PLAYLISTS_ID)
                    .setTitle("Playlists")
                    .setIconUri(
                        Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/playlist_white")
                    )
                    .build(), MediaItem.FLAG_BROWSABLE
            ), MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MEDIA_FAVORITES_ID)
                    .setTitle("Favorites")
                    .setIconUri(
                        Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/heart_white")
                    )
                    .build(), MediaItem.FLAG_BROWSABLE
            ), MediaItem(
                MediaDescription.Builder()
                    .setMediaId(MEDIA_SONGS_ID)
                    .setTitle("Songs")
                    .setIconUri(
                        Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/disc_white")
                    )
                    .build(), MediaItem.FLAG_BROWSABLE
            )
        )
    }

    private val playerConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            playerServiceBinder = service as PlayerService.Binder
            isBound = true
            sessionToken = playerServiceBinder?.mediaSession?.sessionToken
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    private fun isCallerAllowed(
        clientPackageName: String,
        clientUid: Int
    ): Boolean {
        return when {
            clientUid == Process.myUid() -> true
            clientUid == Process.SYSTEM_UID -> true
            ANDROID_AUTO_PACKAGE_NAME == clientPackageName -> true
            else -> false
        }
    }

    companion object {
        const val ANDROID_AUTO_PACKAGE_NAME = "com.google.android.projection.gearhead"
        const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        const val CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1
        const val MEDIA_ROOT_ID = "VIMUSIC_MEDIA_ROOT_ID"
        const val MEDIA_PLAYLISTS_ID = "VIMUSIC_MEDIA_PLAYLISTS_ID"
        const val MEDIA_FAVORITES_ID = "VIMUSIC_MEDIA_FAVORITES_ID"
        const val MEDIA_SONGS_ID = "VIMUSIC_MEDIA_SONGS_ID"
    }

}