package it.vfsfitvnm.vimusic.savers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

interface ResultSaver<Original, Saveable> : Saver<Result<Original>?, Pair<Saveable?, Throwable?>>

fun <Original, Saveable : Any> resultSaver(saver: Saver<Original, Saveable>) =
    object : Saver<Result<Original>?, Pair<Saveable?, Throwable?>> {
        override fun restore(value: Pair<Saveable?, Throwable?>) =
            value.first?.let(saver::restore)?.let(Result.Companion::success)
                ?: value.second?.let(Result.Companion::failure)

        override fun SaverScope.save(value: Result<Original>?) =
            with(saver) { value?.getOrNull()?.let { save(it) } } to value?.exceptionOrNull()
    }
