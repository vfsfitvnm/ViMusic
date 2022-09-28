package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

fun <Original, Saveable : Any> nullableSaver(saver: Saver<Original, Saveable>) =
    object : Saver<Original?, Saveable> {
        override fun SaverScope.save(value: Original?): Saveable? =
            value?.let { with(saver) { save(it) } }

        override fun restore(value: Saveable): Original? =
            saver.restore(value)
    }
