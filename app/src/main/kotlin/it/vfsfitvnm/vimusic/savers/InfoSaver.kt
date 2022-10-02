package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Info

object InfoSaver : Saver<Info, List<String>> {
    override fun SaverScope.save(value: Info): List<String> = listOf(value.id, value.name)

    override fun restore(value: List<String>): Info? {
        return if (value.size == 2) Info(id = value[0], name = value[1]) else null
    }
}

val InfoListSaver = listSaver(InfoSaver)
