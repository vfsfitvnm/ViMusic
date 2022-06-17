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
import android.os.SystemClock
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
import coil.Coil
import coil.request.ImageRequest
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.MainActivity
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.utils.*
import it.vfsfitvnm.youtubemusic.Outcome
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.system.exitProcess


val StartRadioCommand = SessionCommand("StartRadioCommand", Bundle.EMPTY)
val StartArtistRadioCommand = SessionCommand("StartArtistRadioCommand", Bundle.EMPTY)
val StopRadioCommand = SessionCommand("StopRadioCommand", Bundle.EMPTY)

val GetCacheSizeCommand = SessionCommand("GetCacheSizeCommand", Bundle.EMPTY)

val DeleteSongCacheCommand = SessionCommand("DeleteSongCacheCommand", Bundle.EMPTY)

val SetSkipSilenceCommand = SessionCommand("SetSkipSilenceCommand", Bundle.EMPTY)

val GetAudioSessionIdCommand = SessionCommand("GetAudioSessionIdCommand", Bundle.EMPTY)

val SetSleepTimerCommand = SessionCommand("SetSleepTimerCommand", Bundle.EMPTY)
val GetSleepTimerMillisLeftCommand = SessionCommand("GetSleepTimerMillisLeftCommand", Bundle.EMPTY)
val CancelSleepTimerCommand = SessionCommand("CancelSleepTimerCommand", Bundle.EMPTY)


@ExperimentalAnimationApi
@ExperimentalFoundationApi
class PlayerService : MediaSessionService(), MediaSession.Callback, MediaNotification.Provider,
    PlaybackStatsListener.Callback, Player.Listener {

    companion object {
        private const val NotificationId = 1001
        private const val NotificationChannelId = "default_channel_id"

        private const val SleepTimerNotificationId = 1002
        private const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"
    }

    private lateinit var cache: SimpleCache

    private lateinit var player: ExoPlayer

    private lateinit var mediaSession: MediaSession

    private lateinit var notificationManager: NotificationManager

    private var notificationThumbnailSize: Int = 0
    private var lastArtworkUri: Uri? = null
    private var lastBitmap: Bitmap? = null

    private var radio: YoutubePlayer.Radio? = null

    private var sleepTimerJob: Job? = null
    private var sleepTimerRealtime: Long? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    override fun onCreate() {
        super.onCreate()

        notificationThumbnailSize = (256 * resources.displayMetrics.density).roundToInt()

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
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = preferences.repeatMode
        player.skipSilenceEnabled = preferences.skipSilence
        player.playWhenReady = true
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        mediaSession = MediaSession.Builder(this, player)
            .withSessionActivity()
            .setCallback(this)
            .build()

        player.addListener(this)
    }

    override fun onDestroy() {
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
            .add(GetAudioSessionIdCommand)
            .add(SetSleepTimerCommand)
            .add(GetSleepTimerMillisLeftCommand)
            .add(CancelSleepTimerCommand)
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
                return Futures.immediateFuture(
                    SessionResult(
                        SessionResult.RESULT_SUCCESS,
                        bundleOf("cacheSize" to cache.cacheSpace)
                    )
                )
            }
            DeleteSongCacheCommand -> {
                args.getString("videoId")?.let { videoId ->
                    cache.removeResource(videoId)
                }
            }
            SetSkipSilenceCommand -> {
                player.skipSilenceEnabled = args.getBoolean("skipSilence")
            }
            GetAudioSessionIdCommand -> {
                return Futures.immediateFuture(
                    SessionResult(
                        SessionResult.RESULT_SUCCESS,
                        bundleOf("audioSessionId" to player.audioSessionId)
                    )
                )
            }
            SetSleepTimerCommand -> {
                val delayMillis = args.getLong("delayMillis", 2000)

                sleepTimerJob = coroutineScope.launch {
                    sleepTimerRealtime = SystemClock.elapsedRealtime() + delayMillis
                    delay(delayMillis)

                    withContext(Dispatchers.Main) {
                        val notification = NotificationCompat.Builder(
                            this@PlayerService,
                            SleepTimerNotificationChannelId
                        )
                            .setContentTitle("Sleep timer ended")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setShowWhen(true)
                            .setSmallIcon(R.drawable.app_icon)
                            .build()

                        notificationManager.notify(SleepTimerNotificationId, notification)
                    }

                    exitProcess(0)
                }
            }
            GetSleepTimerMillisLeftCommand -> {
                return Futures.immediateFuture(sleepTimerRealtime?.let {
                    (SessionResult(
                        SessionResult.RESULT_SUCCESS,
                        bundleOf("millisLeft" to it - SystemClock.elapsedRealtime())
                    ))
                } ?: SessionResult(SessionResult.RESULT_ERROR_INVALID_STATE))
            }
            CancelSleepTimerCommand -> {
                sleepTimerJob?.cancel()
                sleepTimerJob = null
                sleepTimerRealtime = null
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

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return Futures.immediateFuture(
            mediaItems.map { mediaItem ->
                mediaItem.buildUpon()
                    .setUri(mediaItem.mediaId)
                    .setCustomCacheKey(mediaItem.mediaId)
                    .build()
            }
        )
    }

    override fun createNotification(
        session: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        fun invalidate() {
            onNotificationChangedCallback.onNotificationChanged(
                createNotification(
                    session,
                    customLayout,
                    actionFactory,
                    onNotificationChangedCallback
                )
            )
        }

        fun NotificationCompat.Builder.addMediaAction(
            @DrawableRes resId: Int,
            @StringRes stringId: Int,
            @Player.Command command: Int
        ): NotificationCompat.Builder {
            return addAction(
                actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@PlayerService, resId),
                    getString(stringId),
                    command
                )
            )
        }

        val mediaMetadata = mediaSession.player.mediaMetadata

        val builder = NotificationCompat.Builder(applicationContext, NotificationChannelId)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setLargeIcon(lastBitmap)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.app_icon)
            .setOngoing(false)
            .setContentIntent(mediaSession.sessionActivity)
            .setDeleteIntent(
                actionFactory.createMediaActionPendingIntent(
                    mediaSession,
                    Player.COMMAND_STOP.toLong()
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionCompatToken as android.support.v4.media.session.MediaSessionCompat.Token)
            )
            .addMediaAction(
                R.drawable.play_skip_back,
                R.string.media3_controls_seek_to_previous_description,
                Player.COMMAND_SEEK_TO_PREVIOUS
            ).addMediaAction(
                if (mediaSession.player.playbackState == Player.STATE_ENDED || !mediaSession.player.playWhenReady) R.drawable.play else R.drawable.pause,
                if (mediaSession.player.playbackState == Player.STATE_ENDED || !mediaSession.player.playWhenReady) R.string.media3_controls_play_description else R.string.media3_controls_pause_description,
                Player.COMMAND_PLAY_PAUSE
            )
            .addMediaAction(
                R.drawable.play_skip_forward,
                R.string.media3_controls_seek_to_next_description,
                Player.COMMAND_SEEK_TO_NEXT
            )

        if (lastArtworkUri != mediaMetadata.artworkUri) {
            coroutineScope.launch(Dispatchers.IO) {
                lastBitmap = Coil.imageLoader(applicationContext).execute(
                    ImageRequest.Builder(applicationContext)
                        .data(mediaMetadata.artworkUri.thumbnail(notificationThumbnailSize))
                        .build()
                ).drawable?.let {
                    lastArtworkUri = mediaMetadata.artworkUri
                    (it as BitmapDrawable).bitmap
                } ?: resources.getDrawable(R.drawable.disc_placeholder, null)
                    ?.toBitmap(notificationThumbnailSize, notificationThumbnailSize)

                withContext(Dispatchers.Main) {
                    invalidate()
                }
            }
        }

        return MediaNotification(NotificationId, builder.build())
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean = false

    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Util.SDK_INT < 26) return

        with(notificationManager) {
            if (getNotificationChannel(NotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NotificationChannelId,
                        getString(R.string.default_notification_channel_name),
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
