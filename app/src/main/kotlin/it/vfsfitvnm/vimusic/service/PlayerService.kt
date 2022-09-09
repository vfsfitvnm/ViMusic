package it.vfsfitvnm.vimusic.service

import android.os.Binder as AndroidBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadata
import android.media.audiofx.AudioEffect
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.audio.SonicAudioProcessor
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.MainActivity
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ExoPlayerDiskCacheMaxSize
import it.vfsfitvnm.vimusic.models.QueuedMediaItem
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.utils.InvincibleService
import it.vfsfitvnm.vimusic.utils.RingBuffer
import it.vfsfitvnm.vimusic.utils.TimerJob
import it.vfsfitvnm.vimusic.utils.YouTubeRadio
import it.vfsfitvnm.vimusic.utils.activityPendingIntent
import it.vfsfitvnm.vimusic.utils.broadCastPendingIntent
import it.vfsfitvnm.vimusic.utils.exoPlayerDiskCacheMaxSizeKey
import it.vfsfitvnm.vimusic.utils.findNextMediaItemById
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.getEnum
import it.vfsfitvnm.vimusic.utils.intent
import it.vfsfitvnm.vimusic.utils.isInvincibilityEnabledKey
import it.vfsfitvnm.vimusic.utils.isShowingThumbnailInLockscreenKey
import it.vfsfitvnm.vimusic.utils.mediaItems
import it.vfsfitvnm.vimusic.utils.persistentQueueKey
import it.vfsfitvnm.vimusic.utils.preferences
import it.vfsfitvnm.vimusic.utils.repeatModeKey
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.skipSilenceKey
import it.vfsfitvnm.vimusic.utils.timer
import it.vfsfitvnm.vimusic.utils.volumeNormalizationKey
import it.vfsfitvnm.youtubemusic.YouTube
import it.vfsfitvnm.youtubemusic.models.NavigationEndpoint
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class PlayerService : InvincibleService(), Player.Listener, PlaybackStatsListener.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mediaSession: MediaSession
    private lateinit var cache: SimpleCache
    private lateinit var player: ExoPlayer

    private val stateBuilder = PlaybackState.Builder()
        .setActions(
            PlaybackState.ACTION_PLAY
                    or PlaybackState.ACTION_PAUSE
                    or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackState.ACTION_SKIP_TO_NEXT
                    or PlaybackState.ACTION_PLAY_PAUSE
                    or PlaybackState.ACTION_SEEK_TO
        )

    private val metadataBuilder = MediaMetadata.Builder()

    private var notificationManager: NotificationManager? = null

    private var timerJob: TimerJob? = null

    private var radio: YouTubeRadio? = null

    private lateinit var bitmapProvider: BitmapProvider

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    private var volumeNormalizationJob: Job? = null

    private var isVolumeNormalizationEnabled = false
    private var isPersistentQueueEnabled = false
    private var isShowingThumbnailInLockscreen = true
    override var isInvincibilityEnabled = false

    private val binder = Binder()

    private var isNotificationStarted = false

    override val notificationId: Int
        get() = NotificationId

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    override fun onBind(intent: Intent?): AndroidBinder {
        super.onBind(intent)
        return binder
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

        preferences.registerOnSharedPreferenceChangeListener(this)

        val preferences = preferences
        isPersistentQueueEnabled = preferences.getBoolean(persistentQueueKey, false)
        isVolumeNormalizationEnabled = preferences.getBoolean(volumeNormalizationKey, false)
        isInvincibilityEnabled = preferences.getBoolean(isInvincibilityEnabledKey, false)
        isShowingThumbnailInLockscreen =
            preferences.getBoolean(isShowingThumbnailInLockscreenKey, false)

        val cacheEvictor = when (val size =
            preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }

        // TODO: Remove in a future release
        val directory = cacheDir.resolve("exoplayer").also { directory ->
            if (directory.exists()) return@also

            directory.mkdir()

            cacheDir.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                    if (!file.renameTo(directory.resolve(file.name))) {
                        file.deleteRecursively()
                    }
                }
            }

            filesDir.resolve("coil").deleteRecursively()
        }
        cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(this))

        player = ExoPlayer.Builder(this, createRendersFactory(), createMediaSourceFactory())
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = when (preferences.getInt(repeatModeKey, Player.REPEAT_MODE_ALL)) {
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_ALL
        }
        player.skipSilenceEnabled = preferences.getBoolean(skipSilenceKey, false)
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        maybeRestorePlayerQueue()

        mediaSession = MediaSession(baseContext, "PlayerService")
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true

        notificationActionReceiver = NotificationActionReceiver(player)

        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
        }

        registerReceiver(notificationActionReceiver, filter)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.shouldBePlaying) {
            broadCastPendingIntent<NotificationDismissReceiver>().send()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        maybeSavePlayerQueue()

        preferences.unregisterOnSharedPreferenceChangeListener(this)

        player.removeListener(this)
        player.stop()
        player.release()

        unregisterReceiver(notificationActionReceiver)

        mediaSession.isActive = false
        mediaSession.release()
        cache.release()

        super.onDestroy()
    }

    override fun shouldBeInvincible(): Boolean {
        return !player.shouldBePlaying
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (bitmapProvider.setDefaultBitmap() && player.currentMediaItem != null) {
            notificationManager?.notify(NotificationId, notification())
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        val totalPlayTimeMs = playbackStats.totalPlayTimeMs

        if (totalPlayTimeMs > 2000) {
            query {
                Database.incrementTotalPlayTimeMs(mediaItem.mediaId, totalPlayTimeMs)
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        maybeRecoverPlaybackError()
        maybeNormalizeVolume()
        maybeProcessRadio()

        if (mediaItem == null) {
            bitmapProvider.listener?.invoke(null)
        }
    }

    private fun maybeRecoverPlaybackError() {
        if (player.playerError != null) {
            player.prepare()
        }
    }

    private fun maybeProcessRadio() {
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

        query {
            val queuedSong = Database.queue()
            Database.clearQueue()

            if (queuedSong.isEmpty()) return@query

            val index = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)

            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queuedSong.map { mediaItem ->
                        mediaItem.mediaItem.buildUpon()
                            .setUri(mediaItem.mediaItem.mediaId)
                            .setCustomCacheKey(mediaItem.mediaItem.mediaId)
                            .build()
                    },
                    true
                )
                player.seekTo(index, queuedSong[index].position ?: 0)
                player.prepare()

                isNotificationStarted = true
                startForegroundService(this@PlayerService, intent<PlayerService>())
                startForeground(NotificationId, notification())
            }
        }
    }

    private fun maybeNormalizeVolume() {
        if (!isVolumeNormalizationEnabled) {
            volumeNormalizationJob?.cancel()
            player.volume = 1f
            return
        }

        player.currentMediaItem?.mediaId?.let { songId ->
            volumeNormalizationJob?.cancel()
            volumeNormalizationJob = coroutineScope.launch(Dispatchers.IO) {
                Database.loudnessDb(songId).cancellable().distinctUntilChanged()
                    .collect { loudnessDb ->
                        withContext(Dispatchers.Main) {
                            player.volume = if (loudnessDb != null && loudnessDb > 0) {
                                (1f - (0.01f + loudnessDb / 14)).coerceIn(0.1f, 1f)
                            } else {
                                1f
                            }
                        }
                    }
            }
        }
    }

    private fun maybeShowSongCoverInLockScreen() {
        val bitmap = if (isShowingThumbnailInLockscreen) bitmapProvider.bitmap else null
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
        mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun sendOpenEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
        )
    }

    private fun sendCloseEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
            }
        )
    }

    private val Player.androidPlaybackState: Int
        get() = when (playbackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackState.STATE_BUFFERING else PlaybackState.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackState.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackState.STATE_NONE
            else -> PlaybackState.STATE_NONE
        }

    override fun onRepeatModeChanged(repeatMode: Int) {
        preferences.edit { putInt(repeatModeKey, repeatMode) }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (player.duration != C.TIME_UNSET) {
            metadataBuilder
                .putText(
                    MediaMetadata.METADATA_KEY_TITLE,
                    player.currentMediaItem?.mediaMetadata?.title
                )
                .putText(
                    MediaMetadata.METADATA_KEY_ARTIST,
                    player.currentMediaItem?.mediaMetadata?.artist
                )
                .putText(
                    MediaMetadata.METADATA_KEY_ALBUM,
                    player.currentMediaItem?.mediaMetadata?.albumTitle
                )
                .putLong(MediaMetadata.METADATA_KEY_DURATION, player.duration)
                .build().let(mediaSession::setMetadata)
        }

        stateBuilder
            .setState(player.androidPlaybackState, player.currentPosition, 1f)
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(stateBuilder.build())

        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
            val notification = notification()

            if (notification == null) {
                isNotificationStarted = false
                makeInvincible(false)
                stopForeground(false)
                sendCloseEqualizerIntent()
                notificationManager?.cancel(NotificationId)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                isNotificationStarted = true
                startForegroundService(this@PlayerService, intent<PlayerService>())
                startForeground(NotificationId, notification)
                makeInvincible(false)
                sendOpenEqualizerIntent()
            } else {
                if (!player.shouldBePlaying) {
                    isNotificationStarted = false
                    stopForeground(false)
                    makeInvincible(true)
                    sendCloseEqualizerIntent()
                }
                notificationManager?.notify(NotificationId, notification)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            persistentQueueKey -> isPersistentQueueEnabled =
                sharedPreferences.getBoolean(key, isPersistentQueueEnabled)
            volumeNormalizationKey -> {
                isVolumeNormalizationEnabled =
                    sharedPreferences.getBoolean(key, isVolumeNormalizationEnabled)
                maybeNormalizeVolume()
            }
            isInvincibilityEnabledKey -> isInvincibilityEnabled =
                sharedPreferences.getBoolean(key, isInvincibilityEnabled)
            skipSilenceKey -> player.skipSilenceEnabled = sharedPreferences.getBoolean(key, false)
            isShowingThumbnailInLockscreenKey -> {
                isShowingThumbnailInLockscreen = sharedPreferences.getBoolean(key, true)
                maybeShowSongCoverInLockScreen()
            }
        }
    }

    override fun notification(): Notification? {
        if (player.currentMediaItem == null) return null

        val playIntent = Action.play.pendingIntent
        val pauseIntent = Action.pause.pendingIntent
        val nextIntent = Action.next.pendingIntent
        val prevIntent = Action.previous.pendingIntent

        val mediaMetadata = player.mediaMetadata

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(applicationContext, NotificationChannelId)
        } else {
            Notification.Builder(applicationContext)
        }
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
            .setContentIntent(activityPendingIntent<MainActivity>(
                flags = PendingIntent.FLAG_UPDATE_CURRENT
            ) {
                putExtra("expandPlayerBottomSheet", true)
            })
            .setDeleteIntent(broadCastPendingIntent<NotificationDismissReceiver>())
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                Notification.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addAction(R.drawable.play_skip_back, "Skip back", prevIntent)
            .addAction(
                if (player.shouldBePlaying) R.drawable.pause else R.drawable.play,
                if (player.shouldBePlaying) "Pause" else "Play",
                if (player.shouldBePlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.play_skip_forward, "Skip forward", nextIntent)

        bitmapProvider.load(mediaMetadata.artworkUri) { bitmap ->
            maybeShowSongCoverInLockScreen()
            notificationManager?.notify(NotificationId, builder.setLargeIcon(bitmap).build())
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        notificationManager?.run {
            if (getNotificationChannel(NotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NotificationChannelId,
                        "Now playing",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }

            if (getNotificationChannel(SleepTimerNotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        SleepTimerNotificationChannelId,
                        "Sleep timer",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
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
                            when (val status = body.playabilityStatus.status) {
                                "OK" -> body.streamingData?.adaptiveFormats?.findLast { format ->
                                    format.itag == 251 || format.itag == 140
                                }?.let { format ->
                                    val mediaItem = runBlocking(Dispatchers.Main) {
                                        player.findNextMediaItemById(videoId)
                                    }

                                    query {
                                        mediaItem?.let(Database::insert)

                                        Database.insert(
                                            it.vfsfitvnm.vimusic.models.Format(
                                                songId = videoId,
                                                itag = format.itag,
                                                mimeType = format.mimeType,
                                                bitrate = format.bitrate,
                                                loudnessDb = body.playerConfig?.audioConfig?.loudnessDb?.toFloat(),
                                                contentLength = format.contentLength,
                                                lastModified = format.lastModified
                                            )
                                        )
                                    }

                                    format.url
                                } ?: throw PlayableFormatNotFoundException()
                                "UNPLAYABLE" -> throw UnplayableException()
                                "LOGIN_REQUIRED" -> throw LoginRequiredException()
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
                            urlResult?.exceptionOrNull(),
                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                        )
                    }
                }
            }
        }
    }

    private fun createMediaSourceFactory(): MediaSource.Factory {
        return DefaultMediaSourceFactory(createDataSourceFactory(), createExtractorsFactory())
    }

    private fun createExtractorsFactory(): ExtractorsFactory {
        return ExtractorsFactory {
            arrayOf(MatroskaExtractor(), FragmentedMp4Extractor())
        }
    }

    private fun createRendersFactory(): RenderersFactory {
        val audioSink = DefaultAudioSink.Builder()
            .setEnableFloatOutput(false)
            .setEnableAudioTrackPlaybackParams(false)
            .setOffloadMode(DefaultAudioSink.OFFLOAD_MODE_DISABLED)
            .setAudioProcessorChain(
                DefaultAudioProcessorChain(
                    emptyArray(),
                    SilenceSkippingAudioProcessor(2_000_000, 20_000, 256),
                    SonicAudioProcessor()
                )
            )
            .build()

        return RenderersFactory { handler: Handler?, _, audioListener: AudioRendererEventListener?, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    this,
                    MediaCodecSelector.DEFAULT,
                    handler,
                    audioListener,
                    audioSink
                )
            )
        }
    }

    inner class Binder : AndroidBinder() {
        val player: ExoPlayer
            get() = this@PlayerService.player

        val cache: Cache
            get() = this@PlayerService.cache

        val sleepTimerMillisLeft: StateFlow<Long?>?
            get() = timerJob?.millisLeft

        private var radioJob: Job? = null

        var isLoadingRadio by mutableStateOf(false)
            private set

        fun setBitmapListener(listener: ((Bitmap?) -> Unit)?) {
            bitmapProvider.listener = listener
        }

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

                notificationManager?.notify(SleepTimerNotificationId, notification)

                stopSelf()
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

    private class SessionCallback(private val player: Player) : MediaSession.Callback() {
        override fun onPlay() = player.play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = player.forceSeekToPrevious()
        override fun onSkipToNext() = player.forceSeekToNext()
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
    }

    private class NotificationActionReceiver(private val player: Player) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Action.pause.value -> player.pause()
                Action.play.value -> player.play()
                Action.next.value -> player.forceSeekToNext()
                Action.previous.value -> player.forceSeekToPrevious()
            }
        }
    }

    class NotificationDismissReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.stopService(context.intent<PlayerService>())
        }
    }

    @JvmInline
    private value class Action(val value: String) {
        context(Context)
        val pendingIntent: PendingIntent
            get() = PendingIntent.getBroadcast(
                this@Context,
                100,
                Intent(value).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT.or(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

        companion object {
            val pause = Action("it.vfsfitvnm.vimusic.pause")
            val play = Action("it.vfsfitvnm.vimusic.play")
            val next = Action("it.vfsfitvnm.vimusic.next")
            val previous = Action("it.vfsfitvnm.vimusic.previous")
        }
    }

    private companion object {
        const val NotificationId = 1001
        const val NotificationChannelId = "default_channel_id"

        const val SleepTimerNotificationId = 1002
        const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"
    }
}
