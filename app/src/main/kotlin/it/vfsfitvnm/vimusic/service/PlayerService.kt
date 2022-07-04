package it.vfsfitvnm.vimusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.audio.SonicAudioProcessor
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.MainActivity
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.QueuedMediaItem
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class PlayerService : Service(), Player.Listener, PlaybackStatsListener.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var cache: SimpleCache
    private lateinit var player: ExoPlayer

    private val stateBuilder = Builder()
        .setState(STATE_NONE, 0, 1f)
        .setActions(
            ACTION_PLAY
                    or ACTION_PAUSE
                    or ACTION_SKIP_TO_PREVIOUS
                    or ACTION_SKIP_TO_NEXT
                    or ACTION_PLAY_PAUSE
                    or ACTION_SEEK_TO
        )

    private val metadataBuilder = MediaMetadataCompat.Builder()

    private lateinit var notificationManager: NotificationManager

    private var timerJob: TimerJob? = null

    private var radio: YouTubeRadio? = null

    private lateinit var bitmapProvider: BitmapProvider

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    private val songPendingLoudnessDb = mutableMapOf<String, Float?>()

    private var hack: Hack? = null

    private var isTaskRemoved = false

    private var isVolumeNormalizationEnabled = false
    private var isPersistentQueueEnabled = false

    private val handler = Handler(Looper.getMainLooper())

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                STATE_PLAYING -> {
                    ContextCompat.startForegroundService(
                        this@PlayerService,
                        intent<PlayerService>()
                    )
                    startForeground(NotificationId, notification())

                    hack?.stop()
                }
                STATE_PAUSED -> {
                    if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) {
                        if (isPersistentQueueEnabled) {
                            if (isTaskRemoved) {
                                stopForeground(false)
                            }
                        } else {
                            stopForeground(false)
                        }

                        notificationManager.notify(NotificationId, notification())

                        hack?.start()
                    }
                }
                STATE_NONE -> {
                    onPlaybackStateChanged(Player.STATE_READY)
                    onIsPlayingChanged(player.playWhenReady)
                }
                STATE_ERROR -> {
                    notificationManager.notify(NotificationId, notification())
                }
                else -> {}
            }
        }
    }

    private val binder = Binder()

    override fun onBind(intent: Intent?): Binder {
        hack?.stop()
        hack = null
        return binder
    }

    override fun onRebind(intent: Intent?) {
        isTaskRemoved = false
        hack?.stop()
        hack = null
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hack = Hack()
        return true
    }

    override fun onCreate() {
        super.onCreate()

        bitmapProvider = BitmapProvider(
            bitmapSize = (256 * resources.displayMetrics.density).roundToInt(),
            colorProvider = { isSystemInDarkMode ->
                if (isSystemInDarkMode) Color.BLACK else Color.WHITE
            }
        )

        createNotificationChannel()

        getSharedPreferences(
            Preferences.fileName,
            Context.MODE_PRIVATE
        ).registerOnSharedPreferenceChangeListener(this)

        val preferences = Preferences()
        isPersistentQueueEnabled = preferences.persistentQueue
        isVolumeNormalizationEnabled = preferences.volumeNormalization

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
            .setRenderersFactory(createRendersFactory())
            .setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = preferences.repeatMode
        player.skipSilenceEnabled = preferences.skipSilence
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        maybeRestorePlayerQueue()

        mediaSession = MediaSessionCompat(baseContext, "PlayerService")
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true
        mediaSession.controller.registerCallback(mediaControllerCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        isTaskRemoved = true
        if (!player.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        maybeSavePlayerQueue()

        getSharedPreferences(
            Preferences.fileName,
            Context.MODE_PRIVATE
        ).unregisterOnSharedPreferenceChangeListener(this)

        hack?.stop()
        hack = null

        player.removeListener(this)
        player.stop()
        player.release()

        mediaSession.controller.unregisterCallback(mediaControllerCallback)
        mediaSession.isActive = false
        mediaSession.release()
        cache.release()

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (bitmapProvider.setDefaultBitmap() && player.currentMediaItem != null) {
            notificationManager.notify(NotificationId, notification())
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        query {
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

    private fun maybeSavePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        val mediaItems = player.currentTimeline.mediaItems
        val mediaItemIndex = player.currentMediaItemIndex
        val mediaItemPosition = player.currentPosition

        mediaItems.mapIndexed { index, mediaItem ->
            QueuedMediaItem(
                mediaItem = mediaItem,
                position = if (index == mediaItemIndex) mediaItemPosition else null
            )
        }.let { queuedMediaItems ->
            query {
                Database.clearQueue()
                Database.insert(queuedMediaItems)
            }
        }
    }

    private fun maybeRestorePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        coroutineScope.launch(Dispatchers.IO) {
            val queuedSong = Database.queue()
            Database.clearQueue()

            if (queuedSong.isEmpty()) return@launch

            val index = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)

            withContext(Dispatchers.Main) {
                player.setMediaItems(
                    queuedSong
                        .map { mediaItem ->
                            mediaItem.mediaItem.buildUpon()
                                .setUri(mediaItem.mediaItem.mediaId)
                                .setCustomCacheKey(mediaItem.mediaItem.mediaId)
                                .build()
                        },
                    true
                )
                player.seekTo(index, queuedSong[index].position ?: 0)
                player.prepare()

                ContextCompat.startForegroundService(this@PlayerService, intent<PlayerService>())
                startForeground(NotificationId, notification())
            }
        }
    }

    private fun normalizeVolume() {
        if (isVolumeNormalizationEnabled) {
            player.volume = player.currentMediaItem?.let { mediaItem ->
                songPendingLoudnessDb.getOrElse(mediaItem.mediaId) {
                    mediaItem.mediaMetadata.extras?.getFloatOrNull("loudnessDb")
                }?.takeIf { it > 0 }?.let { loudnessDb ->
                    (1f - (0.01f + loudnessDb / 14)).coerceIn(0.1f, 1f)
                }
            } ?: 1f
        }
    }

    override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            if (player.duration != C.TIME_UNSET) {
                metadataBuilder
                    .putText(
                        MediaMetadataCompat.METADATA_KEY_TITLE,
                        player.currentMediaItem?.mediaMetadata?.title
                    )
                    .putText(
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        player.currentMediaItem?.mediaMetadata?.artist
                    )
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
                mediaSession.setMetadata(metadataBuilder.build())
            }
        }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        if (error != null) {
            stateBuilder
                .setState(STATE_ERROR, player.currentPosition, 1f)

            mediaSession.setPlaybackState(stateBuilder.build())
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        @Player.DiscontinuityReason reason: Int
    ) {
        stateBuilder
            .setState(STATE_NONE, newPosition.positionMs, 1f)
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        stateBuilder
            .setState(if (isPlaying) STATE_PLAYING else STATE_PAUSED, player.currentPosition, 1f)
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Preferences.Keys.persistentQueue -> isPersistentQueueEnabled =
                sharedPreferences.getBoolean(key, isPersistentQueueEnabled)
            Preferences.Keys.volumeNormalization -> isVolumeNormalizationEnabled =
                sharedPreferences.getBoolean(key, isVolumeNormalizationEnabled)
        }
    }

    private fun notification(): Notification {
        fun NotificationCompat.Builder.addMediaAction(
            @DrawableRes resId: Int,
            description: String,
            @MediaKeyAction command: Long
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
            .setSubText(player.playerError?.message)
            .setLargeIcon(bitmapProvider.bitmap)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(player.playerError?.let { R.drawable.alert_circle }
                ?: R.drawable.app_icon)
            .setOngoing(false)
            .setContentIntent(activityPendingIntent<MainActivity>())
            .setDeleteIntent(broadCastPendingIntent<StopServiceBroadcastReceiver>())
            .setChannelId(NotificationChannelId)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addMediaAction(R.drawable.play_skip_back, "Skip back", ACTION_SKIP_TO_PREVIOUS)
            .addMediaAction(
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) R.drawable.play else R.drawable.pause,
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) "Play" else "Pause",
                if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) ACTION_PLAY else ACTION_PAUSE
            )
            .addMediaAction(R.drawable.play_skip_forward, "Skip forward", ACTION_SKIP_TO_NEXT)

        bitmapProvider.load(mediaMetadata.artworkUri) { bitmap ->
            notificationManager.notify(NotificationId, builder.setLargeIcon(bitmap).build())
        }

        return builder.build()
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
                        NotificationManager.IMPORTANCE_DEFAULT
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
                        val urlResult = runBlocking(Dispatchers.IO) {
                            YouTube.player(videoId)
                        }?.mapCatching { body ->
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
                                        player.findNextMediaItemById(videoId)
                                    }

                                    loudnessDb?.let { loudnessDb ->
                                        mediaItem?.mediaMetadata?.extras
                                            ?.putFloat("loudnessDb", loudnessDb)
                                    }

                                    format.contentLength?.let { contentLength ->
                                        mediaItem?.mediaMetadata?.extras
                                            ?.putLong("contentLength", contentLength)
                                    }

                                    mediaItem?.let {
                                        query {
                                            Database.insert(it)
                                        }
                                    }

                                    format.url
                                } ?: throw PlaybackException(
                                    "Couldn't find a playable audio format",
                                    null,
                                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                                )
                                else -> throw PlaybackException(
                                    status,
                                    null,
                                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                                )
                            }
                        }

                        urlResult?.getOrThrow()?.let { url ->
                            ringBuffer.append(videoId to url.toUri())
                            dataSpec.withUri(url.toUri())
                                .subrange(dataSpec.uriPositionOffset, chunkLength)
                        } ?: throw PlaybackException(
                            null,
                            null,
                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                        )
                    }
                }
            }
        }
    }

    private fun createRendersFactory(): RenderersFactory {
        return object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
                enableOffload: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder()
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setOffloadMode(
                        if (enableOffload) {
                            DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_REQUIRED
                        } else {
                            DefaultAudioSink.OFFLOAD_MODE_DISABLED
                        }
                    )
                    .setAudioProcessorChain(
                        DefaultAudioProcessorChain(
                            emptyArray(),
                            SilenceSkippingAudioProcessor(1_000_000, 20_000, 256),
                            SonicAudioProcessor()
                        )
                    )
                    .build()
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

        private var radioJob: Job? = null

        var isLoadingRadio by mutableStateOf(false)
            private set

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

        fun setupRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = true)

        fun playRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = false)

        private fun startRadio(endpoint: NavigationEndpoint.Endpoint.Watch?, justAdd: Boolean) {
            radioJob?.cancel()
            radio = null
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params
            ).let {
                isLoadingRadio = true
                radioJob = coroutineScope.launch(Dispatchers.Main) {
                    if (justAdd) {
                        player.addMediaItems(it.process().drop(1))
                    } else {
                        player.forcePlayFromBeginning(it.process())
                    }
                    radio = it
                    isLoadingRadio = false
                }
            }
        }

        fun stopRadio() {
            isLoadingRadio = false
            radioJob?.cancel()
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

    // https://stackoverflow.com/q/53502244/16885569
    private inner class Hack : Runnable {
        private var isStarted = false
        private val intervalMs = 30_000L

        @Synchronized
        fun start() {
            if (!isStarted) {
                isStarted = true
                handler.postDelayed(this, intervalMs)
            }
        }

        @Synchronized
        fun stop() {
            if (isStarted) {
                handler.removeCallbacks(this)
                isStarted = false
            }
        }

        override fun run() {
            if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) {
                startForeground(NotificationId, notification())
                stopForeground(false)
                handler.postDelayed(this, intervalMs)
            }
        }
    }

    companion object {
        private const val NotificationId = 1001
        private const val NotificationChannelId = "default_channel_id"

        private const val SleepTimerNotificationId = 1002
        private const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"
    }
}

