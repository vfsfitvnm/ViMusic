package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import it.vfsfitvnm.innertube.Innertube

interface ListSaver<Original, Saveable : Any> : Saver<List<Original>, List<Saveable>> {
    override fun SaverScope.save(value: List<Original>): List<Saveable>
    override fun restore(value: List<Saveable>): List<Original>
}

fun <Original, Saveable : Any> resultSaver(saver: Saver<Original, Saveable>) =
    object : Saver<Result<Original>?, Pair<Saveable?, Throwable?>> {
        override fun restore(value: Pair<Saveable?, Throwable?>) =
            value.first?.let(saver::restore)?.let(Result.Companion::success)
                ?: value.second?.let(Result.Companion::failure)

        override fun SaverScope.save(value: Result<Original>?) =
            with(saver) { value?.getOrNull()?.let { save(it) } } to value?.exceptionOrNull()
    }

fun <Original, Saveable : Any> listSaver(saver: Saver<Original, Saveable>) =
    object : ListSaver<Original, Saveable> {
        override fun restore(value: List<Saveable>) =
            value.mapNotNull(saver::restore)

        override fun SaverScope.save(value: List<Original>) =
            with(saver) { value.mapNotNull { save(it) } }
    }

fun <Original, Saveable : Any> nullableSaver(saver: Saver<Original, Saveable>) =
    object : Saver<Original?, Saveable> {
        override fun SaverScope.save(value: Original?): Saveable? =
            value?.let { with(saver) { save(it) } }

        override fun restore(value: Saveable): Original? =
            saver.restore(value)
    }

fun <Original : Innertube.Item> innertubeItemsPageSaver(saver: ListSaver<Original, List<Any?>>) =
    object : Saver<Innertube.ItemsPage<Original>, List<Any?>> {
        override fun SaverScope.save(value: Innertube.ItemsPage<Original>) = listOf(
            value.items?.let { with(saver) { save(it) } },
            value.continuation
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any?>) = Innertube.ItemsPage(
            items = (value[0] as List<List<Any?>>?)?.let(saver::restore),
            continuation = value[1] as String?
        )
    }
