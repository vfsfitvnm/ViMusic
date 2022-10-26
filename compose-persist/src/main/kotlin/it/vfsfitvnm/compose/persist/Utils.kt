package it.vfsfitvnm.compose.persist

import android.content.Context
import android.content.ContextWrapper

val Context.persistMap: PersistMap?
    get() = findOwner<PersistMapOwner>()?.persistMap

internal inline fun <reified T> Context.findOwner(): T? {
    var context = this
    while (context is ContextWrapper) {
        if (context is T) return context
        context = context.baseContext
    }
    return null
}
