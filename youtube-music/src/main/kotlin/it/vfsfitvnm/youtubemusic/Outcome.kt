package it.vfsfitvnm.youtubemusic


sealed class Outcome<out T> {
    val valueOrNull: T?
        get() = when (this) {
            is Success -> value
            is Recovered -> value
            else -> null
        }

    fun recoverWith(value: @UnsafeVariance T): Outcome<T> {
        return when (this) {
            is Error -> Recovered(value, this)
            else -> this
        }
    }

    inline fun <R> map(block: (T) -> R): Outcome<R> {
        return when (this) {
            is Success -> Success(block(value))
            is Recovered -> Success(block(value))
            is Initial -> this
            is Loading -> this
            is Error -> this
        }
    }

    inline fun <R> flatMap(block: (T) -> Outcome<R>): Outcome<R> {
        return when (this) {
            is Success -> block(value)
            is Recovered -> block(value)
            is Initial -> this
            is Loading -> this
            is Error -> this
        }
    }

    object Initial : Outcome<Nothing>()

    object Loading : Outcome<Nothing>()

    sealed class Error : Outcome<Nothing>() {
        object Network : Error()
        class Unhandled(val throwable: Throwable) : Error()
    }

    class Recovered<T>(val value: T, val error: Error) : Outcome<T>()
    
    class Success<T>(val value: T) : Outcome<T>()
}

fun <T> Outcome<T>?.toNotNull(): Outcome<T?> {
    return when (this) {
        null -> Outcome.Success(null)
        else -> this
    }
}

fun <T> Outcome<T?>.toNullable(error: Outcome.Error? = null): Outcome<T>? {
    return when (this) {
        is Outcome.Success -> value?.let { Outcome.Success(it) } ?: error
        is Outcome.Recovered -> value?.let { Outcome.Success(it) } ?: error
        is Outcome.Initial -> this
        is Outcome.Loading -> this
        is Outcome.Error -> this
    }
}

val Outcome<*>.isEvaluable: Boolean
    get() = this !is Outcome.Success && this !is Outcome.Loading

