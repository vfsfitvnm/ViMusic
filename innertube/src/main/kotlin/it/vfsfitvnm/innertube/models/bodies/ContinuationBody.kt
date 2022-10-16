package it.vfsfitvnm.innertube.models.bodies

import it.vfsfitvnm.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String,
)
