package it.vfsfitvnm.vimusic

import android.app.Application
import android.content.ComponentName
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.google.common.util.concurrent.ListenableFuture
import it.vfsfitvnm.vimusic.services.PlayerService
import it.vfsfitvnm.vimusic.utils.preferences


@ExperimentalAnimationApi
@ExperimentalFoundationApi
class MainApplication : Application(), ImageLoaderFactory {
    lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    override fun onCreate() {
        super.onCreate()

        DatabaseInitializer()

        val sessionToken = SessionToken(this, ComponentName(this, PlayerService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .diskCache(
                DiskCache.Builder()
                    .directory(filesDir.resolve("coil"))
                    .maxSizeBytes(preferences.coilDiskCacheMaxSizeBytes)
                    .build()
            )
            .build()
    }
}
