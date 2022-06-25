package it.vfsfitvnm.vimusic.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

inline fun <reified T> Context.intent(): Intent =
    Intent(this@Context, T::class.java)

inline fun <reified T: BroadcastReceiver> Context.broadCastPendingIntent(
    requestCode: Int = 0,
    flags: Int = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0,
): PendingIntent =
    PendingIntent.getBroadcast(this, requestCode, intent<T>(), flags)

inline fun <reified T: Activity> Context.activityPendingIntent(
    requestCode: Int = 0,
    flags: Int = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0,
): PendingIntent =
    PendingIntent.getActivity(this, requestCode, intent<T>(), flags)