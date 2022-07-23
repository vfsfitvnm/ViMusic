package it.vfsfitvnm.vimusic.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface TimerJob {
    val millisLeft: StateFlow<Long?>
    fun cancel()
}

fun CoroutineScope.timer(delayMillis: Long, onCompletion: () -> Unit): TimerJob {
    val millisLeft = MutableStateFlow<Long?>(delayMillis)
    val job = launch {
        while (isActive && millisLeft.value != null) {
            delay(1000)
            millisLeft.emit(millisLeft.value?.minus(1000)?.takeIf { it > 0 })
        }
    }
    val disposableHandle = job.invokeOnCompletion {
        if (it == null) {
            onCompletion()
        }
    }

    return object : TimerJob {
        override val millisLeft: StateFlow<Long?>
            get() = millisLeft.asStateFlow()

        override fun cancel() {
            millisLeft.value = null
            disposableHandle.dispose()
            job.cancel()
        }
    }
}
