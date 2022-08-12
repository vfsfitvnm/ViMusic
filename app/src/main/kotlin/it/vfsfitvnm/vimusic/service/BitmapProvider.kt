package it.vfsfitvnm.vimusic.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.graphics.applyCanvas
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import it.vfsfitvnm.vimusic.utils.thumbnail

context(Context)
class BitmapProvider(
    private val bitmapSize: Int,
    private val colorProvider: (isSystemInDarkMode: Boolean) -> Int
) {
    private var lastUri: Uri? = null
    var lastBitmap: Bitmap? = null
    private var lastIsSystemInDarkMode = false

    private var lastEnqueued: Disposable? = null

    private lateinit var defaultBitmap: Bitmap

    val bitmap: Bitmap
        get() = lastBitmap ?: defaultBitmap

    var listener: ((Bitmap?) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(lastBitmap)
        }

    init {
        setDefaultBitmap()
    }

    fun setDefaultBitmap(): Boolean {
        val isSystemInDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (::defaultBitmap.isInitialized && isSystemInDarkMode == lastIsSystemInDarkMode) return false

        lastIsSystemInDarkMode = isSystemInDarkMode

        defaultBitmap =
            Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888).applyCanvas {
                drawColor(colorProvider(isSystemInDarkMode))
            }

        return lastBitmap == null
    }

    fun load(uri: Uri?, onDone: (Bitmap) -> Unit) {
        if (lastUri == uri) return

        lastEnqueued?.dispose()
        lastUri = uri

        lastEnqueued = applicationContext.imageLoader.enqueue(
            ImageRequest.Builder(applicationContext)
                .data(uri.thumbnail(bitmapSize))
                .allowHardware(false)
                .listener(
                    onError = { _, _ ->
                        lastBitmap = null
                        onDone(bitmap)
                        listener?.invoke(lastBitmap)
                    },
                    onSuccess = { _, result ->
                        lastBitmap = (result.drawable as BitmapDrawable).bitmap
                        onDone(bitmap)
                        listener?.invoke(lastBitmap)
                    }
                )
                .build()
        )
    }
}
