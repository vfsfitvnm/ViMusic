package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.Info

object InfoSaver : Saver<Info, List<String?>> {
    override fun SaverScope.save(value: Info) = listOf(value.id, value.name)

    override fun restore(value: List<String?>) = Info(id = value[0] as String, name = value[1])
}

val InfoListSaver = listSaver(InfoSaver)
