package it.vfsfitvnm.vimusic

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache


class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()
    }

    override fun newImageLoader(): ImageLoader {
        return defaultCoilImageLoader(1024 * 1024 * 1024)
    }
}

fun Context.defaultCoilImageLoader(diskCacheMaxSize: Long): ImageLoader {
    return ImageLoader.Builder(this)
        .crossfade(true)
        .diskCache(
            DiskCache.Builder()
                .directory(filesDir.resolve("coil"))
                .maxSizeBytes(diskCacheMaxSize)
                .build()
        )
        .build()
}