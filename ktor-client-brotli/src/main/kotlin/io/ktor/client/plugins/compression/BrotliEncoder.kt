package io.ktor.client.plugins.compression

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CoroutineScope
import org.brotli.dec.BrotliInputStream

internal object BrotliEncoder : ContentEncoder {
    override val name: String = "br"

    override fun CoroutineScope.encode(source: ByteReadChannel) =
        error("BrotliOutputStream not available (https://github.com/google/brotli/issues/715)")

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel =
        BrotliInputStream(source.toInputStream()).toByteReadChannel()
}
