package it.vfsfitvnm.vimusic.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.getSystemService

inline fun <reified T> Context.intent(): Intent =
    Intent(this@Context, T::class.java)

inline fun <reified T : BroadcastReceiver> Context.broadCastPendingIntent(
    requestCode: Int = 0,
    flags: Int = if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0,
): PendingIntent =
    PendingIntent.getBroadcast(this, requestCode, intent<T>(), flags)

inline fun <reified T : Activity> Context.activityPendingIntent(
    requestCode: Int = 0,
    flags: Int = 0,
    block: Intent.() -> Unit = {},
): PendingIntent =
    PendingIntent.getActivity(
        this,
        requestCode,
        intent<T>().apply(block),
        (if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0) or flags
    )

val Context.isIgnoringBatteryOptimizations: Boolean
    get() = if (isAtLeastAndroid6) {
        getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) ?: true
    } else {
        true
    }

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
