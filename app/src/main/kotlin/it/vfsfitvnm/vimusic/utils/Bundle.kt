package it.vfsfitvnm.vimusic.utils

import android.os.Bundle


fun Bundle.getFloatOrNull(key: String): Float? =
    if (containsKey(key)) getFloat(key) else null


fun Bundle.getLongOrNull(key: String): Long? =
    if (containsKey(key)) getLong(key) else null
