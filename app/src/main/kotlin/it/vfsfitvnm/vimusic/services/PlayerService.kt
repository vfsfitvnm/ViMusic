package it.vfsfitvnm.vimusic.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.*
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import coil.Coil
import coil.request.ImageRequest
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.MainActivity
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.internal
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class PlayerService : Service(), Player.Listener, PlaybackStatsListener.Callback {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var cache: SimpleCache
    private lateinit var player: ExoPlayer

    private val stateBuilder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SEEK_TO
        )

    private val metadataBuilder = MediaMetadataCompat.Builder()

    private lateinit var notificationManager: NotificationManager

    private var timerJob: TimerJob? = null

    private var notificationThumbnailSize: Int = 0
    private var lastArtworkUri: Uri? = null
    private var lastBitmap: Bitmap? = null

    private var radio: YoutubePlayer.Radio? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    private val songPendingLoudnessDb = mutableMapOf<String, Float?>()

    override fun onBind(intent: Intent?) = Binder()

    override fun onCreate() {
        super.onCreate()

        notificationThumbnailSize = (256 * resources.displayMetrics.density).roundToInt()

        lastBitmap = resources.getDrawable(R.drawable.disc_placeholder, null)
            ?.toBitmap(notificationThumbnailSize, notificationThumbnailSize)

        createNotificationChannel()

        val cacheEvictor = LeastRecentlyUsedCacheEvictor(preferences.exoPlayerDiskCacheMaxSizeBytes)
        cache = SimpleCache(cacheDir, cacheEvictor, StandaloneDatabaseProvider(this))

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setMediaSourceFactory(DefaultMediaSourceFactory(createDataSourceFactory()))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = preferences.repeatMode
        player.skipSilenceEnabled = preferences.skipSilence
        player.playWhenReady = true
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        mediaSession = MediaSessionCompat(this, "PlayerService")
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            notificationManager.cancel(NotificationId)
            stopSelf()
        }

        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        player.removeListener(this)
        player.stop()
        player.release()
        mediaSession.isActive = false
        mediaSession.release()
        cache.release()
        super.onDestroy()
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        Database.internal.queryExecutor.execute {
            Database.incrementTotalPlayTimeMs(mediaItem.mediaId, playbackStats.totalPlayTimeMs)
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        normalizeVolume()

        radio?.let { radio ->
            if (player.mediaItemCount - player.currentMediaItemIndex <= 3) {
                coroutineScope.launch(Dispatchers.Main) {
                    player.addMediaItems(radio.process())
                }
            }
        }
    }

    private fun normalizeVolume() {
        if (preferences.volumeNormalization) {
            player.volume = player.currentMediaItem?.mediaId?.let { mediaId ->
                songPendingLoudnessDb.getOrElse(mediaId) {
                    player.currentMediaItem?.mediaMetadata?.extras?.getFloat("loudnessDb")
                }?.takeIf { it > 0 }?.let { loudnessDb ->
                    (1f - (0.01f + loudnessDb / 14)).coerceIn(0.1f, 1f)
                }
            } ?: 1f
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        @Player.DiscontinuityReason reason: Int
    ) {
        stateBuilder
            .setState(PlaybackStateCompat.STATE_NONE, newPosition.positionMs, 1f)
            .setBufferedPosition(player.bufferedPosition)

        updateNotification()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        stateBuilder
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                player.currentPosition,
                1f
            )
            .setBufferedPosition(player.bufferedPosition)

        updateNotification()
    }

    private fun updateNotification() {
        if (player.duration != C.TIME_UNSET) {
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
            mediaSession.setMetadata(metadataBuilder.build())
        }
        mediaSession.setPlaybackState(stateBuilder.build())
        createNotification()
    }

    private fun createNotification() {
        fun NotificationCompat.Builder.addMediaAction(
            @DrawableRes resId: Int,
            description: String,
            @PlaybackStateCompat.MediaKeyAction command: Long
        ): NotificationCompat.Builder {
            return addAction(
                NotificationCompat.Action(
                    resId,
                    description,
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this@PlayerService, command)
                )
            )
        }

        val mediaMetadata = player.mediaMetadata

        val builder = NotificationCompat.Builder(applicationContext, NotificationChannelId)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setLargeIcon(lastBitmap)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.app_icon)
            .setOngoing(false)
            .setContentIntent(activityPendingIntent<MainActivity>())
            .setDeleteIntent(broadCastPendingIntent<StopServiceBroadcastReceiver>())
            .setChannelId(NotificationChannelId)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addMediaAction(
                R.drawable.play_skip_back,
                "Skip back",
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            ).addMediaAction(
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) R.drawable.play else R.drawable.pause,
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) "Play" else "Pause",
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) PlaybackStateCompat.ACTION_PLAY else PlaybackStateCompat.ACTION_PAUSE
            )
            .addMediaAction(
                R.drawable.play_skip_forward,
                "Skip forward",
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )

        if (lastArtworkUri != mediaMetadata.artworkUri) {
            lastArtworkUri = mediaMetadata.artworkUri

            Coil.imageLoader(applicationContext).enqueue(
                ImageRequest.Builder(applicationContext)
                    .data(mediaMetadata.artworkUri.thumbnail(notificationThumbnailSize))
                    .listener(
                        onError = { _, _ ->
                            lastBitmap = resources.getDrawable(R.drawable.disc_placeholder, null)
                                ?.toBitmap(notificationThumbnailSize, notificationThumbnailSize)
                            notificationManager.notify(NotificationId, builder.setLargeIcon(lastBitmap).build())
                        },
                        onSuccess = { _, result ->
                            lastBitmap = (result.drawable as BitmapDrawable).bitmap
                            notificationManager.notify(NotificationId, builder.setLargeIcon(lastBitmap).build())
                        }
                    )
                    .build()
            )
        }

        val notificationCompat = builder.build()
        startForeground(NotificationId, notificationCompat)

        if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                stopForeground(false)
            }
            notificationManager.notify(NotificationId, notificationCompat)
        }
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        with(notificationManager) {
            if (getNotificationChannel(NotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NotificationChannelId,
                        "Now playing",
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }

            if (getNotificationChannel(SleepTimerNotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        SleepTimerNotificationChannelId,
                        "Sleep timer",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
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
                            val loudnessDb = body.playerConfig?.audioConfig?.loudnessDb?.toFloat()

                            songPendingLoudnessDb[videoId] = loudnessDb

                            runBlocking(Dispatchers.Main) {
                                normalizeVolume()
                            }

                            when (val status = body.playabilityStatus.status) {
                                "OK" -> body.streamingData?.adaptiveFormats?.findLast { format ->
                                    format.itag == 251 || format.itag == 140
                                }?.let { format ->
                                    val mediaItem = runBlocking(Dispatchers.Main) {
                                        player.currentMediaItem
                                    }

                                    if (mediaItem?.mediaId == videoId) {
                                        Database.internal.queryExecutor.execute {
                                            Database.update(
                                                Database.insert(mediaItem).copy(
                                                    loudnessDb = loudnessDb,
                                                    contentLength = format.contentLength
                                                )
                                            )
                                        }
                                    }

                                    Outcome.Success(format.url)
                                } ?: Outcome.Error.Unhandled(
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

    inner class Binder : android.os.Binder() {
        val player: ExoPlayer
            get() = this@PlayerService.player

        val cache: Cache
            get() = this@PlayerService.cache

        val sleepTimerMillisLeft: StateFlow<Long?>?
            get() = timerJob?.millisLeft

        fun startSleepTimer(delayMillis: Long) {
            timerJob?.cancel()

            timerJob = coroutineScope.timer(delayMillis) {
                val notification = NotificationCompat
                    .Builder(this@PlayerService, SleepTimerNotificationChannelId)
                    .setContentTitle("Sleep timer ended")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.app_icon)
                    .build()

                notificationManager.notify(SleepTimerNotificationId, notification)

                exitProcess(0)
            }
        }

        fun cancelSleepTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        fun startRadio(
            endpoint: NavigationEndpoint.Endpoint.Watch?,
        ) {
            startRadio(
                videoId = endpoint?.videoId,
                playlistId = endpoint?.playlistId,
                playlistSetVideoId = endpoint?.playlistSetVideoId,
                parameters = endpoint?.params,
                justAdd = false
            )
        }

        fun startRadio(
            videoId: String?,
            playlistId: String? = null,
            playlistSetVideoId: String? = null,
            parameters: String? = null,
            justAdd: Boolean = true
        ) {
            radio = null
            YoutubePlayer.Radio(
                videoId, playlistId, playlistSetVideoId, parameters
            ).let {
                coroutineScope.launch(Dispatchers.Main) {
                    if (justAdd) {
                        player.addMediaItems(it.process().drop(1))
                    } else {
                        player.forcePlayFromBeginning(it.process())
                    }
                    radio = it
                }
            }
        }

        fun stopRadio() {
            radio = null
        }
    }

    private class SessionCallback(private val player: Player) : MediaSessionCompat.Callback() {
        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = player.seekToPrevious()
        override fun onSkipToNext() = player.seekToNext()
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
    }

    class StopServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.stopService(context.intent<PlayerService>())
        }
    }

    companion object {
        private const val NotificationId = 1001
        private const val NotificationChannelId = "default_channel_id"

        private const val SleepTimerNotificationId = 1002
        private const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"
    }
}
