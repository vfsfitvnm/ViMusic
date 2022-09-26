package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

interface ListSaver<Original, Saveable : Any> : Saver<List<Original>, List<Saveable>> {
    companion object {
        fun <Original, Saveable : Any> of(saver: Saver<Original, Saveable>): ListSaver<Original, Saveable> {
            return object : ListSaver<Original, Saveable> {
                override fun restore(value: List<Saveable>): List<Original> {
                    return value.mapNotNull(saver::restore)
                }

                override fun SaverScope.save(value: List<Original>): List<Saveable> {
                    return with(saver) { value.mapNotNull { save(it) } }
                }
            }
        }
    }
}
