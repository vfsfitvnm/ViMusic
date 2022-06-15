package it.vfsfitvnm.vimusic.utils

import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.guava.await


suspend fun MediaController.send(command: SessionCommand, args: Bundle = Bundle.EMPTY): SessionResult {
    return sendCustomCommand(command, args).await()
}

fun MediaController.command(command: SessionCommand, args: Bundle = Bundle.EMPTY, listener: ((SessionResult) -> Unit)? = null) {
    val future = sendCustomCommand(command, args)
    listener?.let {
        future.addListener({ it(future.get()) }, MoreExecutors.directExecutor())
    }
}

fun MediaController.syncCommand(command: SessionCommand, args: Bundle = Bundle.EMPTY): SessionResult {
    return sendCustomCommand(command, args).get()
}