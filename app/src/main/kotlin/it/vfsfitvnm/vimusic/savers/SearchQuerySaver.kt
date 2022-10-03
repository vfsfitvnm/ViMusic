package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.vimusic.models.SearchQuery

object SearchQuerySaver : Saver<SearchQuery, List<Any?>> {
    override fun SaverScope.save(value: SearchQuery): List<Any?> = listOf(
        value.id,
        value.query,
    )

    override fun restore(value: List<Any?>) = SearchQuery(
        id = value[0] as Long,
        query = value[1] as String
    )
}
