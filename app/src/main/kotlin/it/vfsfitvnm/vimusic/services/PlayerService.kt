package it.vfsfitvnm.vimusic.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.*
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.*
import androidx.media3.session.MediaNotification.ActionFactory
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import it.vfsfitvnm.vimusic.*
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.QueuedMediaItem
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import kotlinx.coroutines.*
import kotlin.math.roundToInt


val StartRadioCommand = SessionCommand("StartRadioCommand", Bundle.EMPTY)
val StartArtistRadioCommand = SessionCommand("StartArtistRadioCommand", Bundle.EMPTY)
val StopRadioCommand = SessionCommand("StopRadioCommand", Bundle.EMPTY)

val GetCacheSizeCommand = SessionCommand("GetCacheSizeCommand", Bundle.EMPTY)

val DeleteSongCacheCommand = SessionCommand("DeleteSongCacheCommand", Bundle.EMPTY)

val SetSkipSilenceCommand = SessionCommand("SetSkipSilenceCommand", Bundle.EMPTY)

@ExperimentalAnimationApi
@ExperimentalFoundationApi
class PlayerService : MediaSessionService(), MediaSession.MediaItemFiller,
    MediaNotification.Provider,
    MediaSession.SessionCallback,
    PlaybackStatsListener.Callback, Player.Listener {

    companion object {
        private const val NotificationId = 1001
        private const val NotificationChannelId = "default_channel_id"
    }

    private lateinit var cache: SimpleCache

    private lateinit var player: ExoPlayer

    private lateinit var mediaSession: MediaSession

    private lateinit var notificationManager: NotificationManager

    private var lastArtworkUri: Uri? = null
    private var lastBitmap: Bitmap? = null

    private var radio: YoutubePlayer.Radio? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        setMediaNotificationProvider(this)

        val cacheEvictor = LeastRecentlyUsedCacheEvictor(preferences.exoPlayerDiskCacheMaxSizeBytes)
        cache = SimpleCache(cacheDir, cacheEvictor, StandaloneDatabaseProvider(this))

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setMediaSourceFactory(DefaultMediaSourceFactory(createDataSourceFactory()))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()

        player.repeatMode = preferences.repeatMode
        player.skipSilenceEnabled = preferences.skipSilence
        player.playWhenReady = true
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        mediaSession = MediaSession.Builder(this, player)
            .withSessionActivity()
            .setSessionCallback(this)
            .setMediaItemFiller(this)
            .build()

        player.addListener(this)

        if (preferences.persistentQueue) {
            coroutineScope.launch(Dispatchers.IO) {
                val queuedMediaItems = Database.queuedMediaItems()
                Database.clearQueuedMediaItems()

                if (queuedMediaItems.isEmpty()) return@launch

                val index = queuedMediaItems.indexOfFirst { it.position != null }.coerceAtLeast(0)

                withContext(Dispatchers.Main) {
                    player.setMediaItems(
                        queuedMediaItems
                            .map(QueuedMediaItem::mediaItem)
                            .map { mediaItem ->
                                mediaItem.buildUpon()
                                    .setUri(mediaItem.mediaId)
                                    .setCustomCacheKey(mediaItem.mediaId)
                                    .build()
                            },
                        true
                    )
                    player.seekTo(index, queuedMediaItems[index].position ?: 0)
                    player.playWhenReady = false
                    player.prepare()
                }
            }
        }
    }

    override fun onDestroy() {
        if (preferences.persistentQueue) {
            val mediaItems = player.currentTimeline.mediaItems
            val mediaItemIndex = player.currentMediaItemIndex
            val mediaItemPosition = player.currentPosition

            Database.internal.queryExecutor.execute {
                Database.clearQueuedMediaItems()
                Database.insertQueuedMediaItems(
                    mediaItems.mapIndexed { index, mediaItem ->
                        QueuedMediaItem(
                            mediaItem = mediaItem,
                            position = if (index == mediaItemIndex) mediaItemPosition else null
                        )
                    }
                )
            }
        }

        player.release()
        mediaSession.release()
        cache.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val sessionCommands = SessionCommands.Builder()
            .add(StartRadioCommand)
            .add(StartArtistRadioCommand)
            .add(StopRadioCommand)
            .add(GetCacheSizeCommand)
            .add(DeleteSongCacheCommand)
            .add(SetSkipSilenceCommand)
            .build()
        val playerCommands = Player.Commands.Builder().addAllCommands().build()
        return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand) {
            StartRadioCommand, StartArtistRadioCommand -> {
                radio = null
                YoutubePlayer.Radio(
                    videoId = args.getString("videoId"),
                    playlistId = args.getString("playlistId"),
                    playlistSetVideoId = args.getString("playlistSetVideoId"),
                    parameters = args.getString("params"),
                ).let {
                    coroutineScope.launch(Dispatchers.Main) {
                        when (customCommand) {
                            StartRadioCommand -> player.addMediaItems(it.process().drop(1))
                            StartArtistRadioCommand -> player.forcePlayFromBeginning(it.process())
                        }
                        radio = it
                    }
                }
            }
            StopRadioCommand -> radio = null
            GetCacheSizeCommand -> {
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, bundleOf("cacheSize" to cache.cacheSpace)))
            }
            DeleteSongCacheCommand -> {
                args.getString("videoId")?.let { videoId ->
                    cache.removeResource(videoId)
                }
            }
            SetSkipSilenceCommand -> {
                player.skipSilenceEnabled = args.getBoolean("skipSilence")
            }
        }

        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        coroutineScope.launch(Dispatchers.IO) {
            Database.insert(mediaItem)
            Database.incrementTotalPlayTimeMs(mediaItem.mediaId, playbackStats.totalPlayTimeMs)
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        radio?.let { radio ->
            if (player.mediaItemCount - player.currentMediaItemIndex <= 3) {
                coroutineScope.launch(Dispatchers.Main) {
                    player.addMediaItems(radio.process())
                }
            }
        }
    }

    override fun fillInLocalConfiguration(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItem: MediaItem
    ): MediaItem {
        return mediaItem.buildUpon()
            .setUri(mediaItem.mediaId)
            .setCustomCacheKey(mediaItem.mediaId)
            .build()
    }

    override fun createNotification(
        mediaController: MediaController,
        actionFactory: ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        fun NotificationCompat.Builder.addMediaAction(
            @DrawableRes resId: Int,
            @StringRes stringId: Int,
            @Player.Command command: Long
        ): NotificationCompat.Builder {
            return addAction(
                actionFactory.createMediaAction(
                    IconCompat.createWithResource(this@PlayerService, resId),
                    getString(stringId),
                    command
                )
            )
        }

        val mediaMetadata = mediaController.mediaMetadata

        val builder = NotificationCompat.Builder(applicationContext, NotificationChannelId)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .addMediaAction(
                R.drawable.play_skip_back,
                R.string.media3_controls_seek_to_previous_description,
                ActionFactory.COMMAND_SKIP_TO_PREVIOUS
            ).run {
                if (mediaController.playbackState == Player.STATE_ENDED || !mediaController.playWhenReady) {
                    addMediaAction(
                        R.drawable.play,
                        R.string.media3_controls_play_description,
                        ActionFactory.COMMAND_PLAY
                    )
                } else {
                    addMediaAction(
                        R.drawable.pause,
                        R.string.media3_controls_pause_description,
                        ActionFactory.COMMAND_PAUSE
                    )
                }
            }.addMediaAction(
                R.drawable.play_skip_forward,
                R.string.media3_controls_seek_to_next_description,
                ActionFactory.COMMAND_SKIP_TO_NEXT
            )
            .setContentIntent(mediaController.sessionActivity)
            .setDeleteIntent(
                actionFactory.createMediaActionPendingIntent(
                    ActionFactory.COMMAND_STOP
                )
            )
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.app_icon)
            .setOngoing(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionCompatToken as android.support.v4.media.session.MediaSessionCompat.Token)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        if (lastArtworkUri == mediaMetadata.artworkUri) {
            builder.setLargeIcon(lastBitmap)
        } else {
            val size = (96 * resources.displayMetrics.density).roundToInt()

            builder.setLargeIcon(
                resources.getDrawable(R.drawable.disc_placeholder, null)?.toBitmap(size, size)
            )

            ImageLoader(applicationContext)
                .enqueue(
                    ImageRequest.Builder(applicationContext)
                        .listener { _, result ->
                            lastBitmap = (result.drawable as BitmapDrawable).bitmap
                            lastArtworkUri = mediaMetadata.artworkUri

                            onNotificationChangedCallback.onNotificationChanged(
                                MediaNotification(
                                    NotificationId,
                                    builder.setLargeIcon(lastBitmap).build()
                                )
                            )
                        }
                        .data("${mediaMetadata.artworkUri}-w${size}-h${size}")
                        .build()
                )
        }

        return MediaNotification(NotificationId, builder.build())
    }

    override fun handleCustomAction(
        mediaController: MediaController,
        action: String,
        extras: Bundle
    ) = Unit

    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Util.SDK_INT >= 26 && notificationManager.getNotificationChannel(NotificationChannelId) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NotificationChannelId,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun createCacheDataSource(): DataSource.Factory {
        return CacheDataSource.Factory().setCache(cache).apply {
            setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
            )
        }
    }

    private fun createDataSourceFactory(): DataSource.Factory {
        val chunkLength = 512 * 1024L
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }

        return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
            val videoId = dataSpec.key ?: error("A key must be set")

            if (cache.isCached(videoId, dataSpec.position, chunkLength)) {
                dataSpec
            } else {
                when (videoId) {
                    ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                    ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                    else -> {
                        val url = runBlocking(Dispatchers.IO) {
                            it.vfsfitvnm.youtubemusic.YouTube.player(videoId)
                        }.flatMap { body ->
                            when (val status = body.playabilityStatus.status) {
                                "OK" -> body.streamingData?.adaptiveFormats?.findLast { format ->
                                    format.itag == 251 || format.itag == 140
                                }?.url?.let { Outcome.Success(it) } ?: Outcome.Error.Unhandled(
                                    PlaybackException(
                                        "Couldn't find a playable audio format",
                                        null,
                                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                                    )
                                )
                                else -> Outcome.Error.Unhandled(
                                    PlaybackException(
                                        status,
                                        null,
                                        PlaybackException.ERROR_CODE_REMOTE_ERROR
                                    )
                                )
                            }
                        }

                        when (url) {
                            is Outcome.Success -> {
                                ringBuffer.append(videoId to url.value.toUri())
                                dataSpec.withUri(url.value.toUri())
                                    .subrange(dataSpec.uriPositionOffset, chunkLength)
                            }
                            is Outcome.Error.Network -> throw PlaybackException(
                                "Couldn't reach the internet",
                                null,
                                PlaybackException.ERROR_CODE_REMOTE_ERROR
                            )
                            is Outcome.Error.Unhandled -> throw url.throwable
                            else -> throw PlaybackException(
                                "Unexpected error",
                                null,
                                PlaybackException.ERROR_CODE_REMOTE_ERROR
                            )
                        }
                    }
                }
            }
        }
    }

    private fun MediaSession.Builder.withSessionActivity(): MediaSession.Builder {
        return setSessionActivity(
            PendingIntent.getActivity(
                this@PlayerService,
                0,
                Intent(this@PlayerService, MainActivity::class.java),
                if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
            )
        )
    }
}
