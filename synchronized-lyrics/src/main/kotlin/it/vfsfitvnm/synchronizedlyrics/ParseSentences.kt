package it.vfsfitvnm.synchronizedlyrics

fun parseSentences(text: String): List<Pair<Long, String>> {
    return mutableListOf(0L to "").apply {
        for (line in text.trim().lines()) {
            val sentence = line.substring(10)

            if (sentence.startsWith(" 作词 : ") || sentence.startsWith(" 作曲 : ")) {
                continue
            }

            val position = line.take(10).run {
                get(8).digitToInt() * 10L +
                        get(7).digitToInt() * 100 +
                        get(5).digitToInt() * 1000 +
                        get(4).digitToInt() * 10000 +
                        get(2).digitToInt() * 60 * 1000 +
                        get(1).digitToInt() * 600 * 1000
            }

            add(position to sentence)
        }
    }
}
