package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Runs(
    val runs: List<Run> = listOf()
) {
    val text: String
        get() = runs.joinToString("") { it.text ?: "" }

    fun splitBySeparator(): List<List<Run>> {
        return runs.flatMapIndexed { index, run ->
            when {
                index == 0 || index == runs.lastIndex -> listOf(index)
                run.text == " • " -> listOf(index - 1, index + 1)
                else -> emptyList()
            }
        }.windowed(size = 2, step = 2) { (from, to) -> runs.slice(from..to) }.let {
            it.ifEmpty {
                listOf(runs)
            }
        }
    }

    @Serializable
    data class Run(
        val text: String?,
        val navigationEndpoint: NavigationEndpoint?,
    )
}
