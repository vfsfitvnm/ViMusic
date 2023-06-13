package it.vfsfitvnm.innertube.utils

import java.net.Proxy

object ProxyPreferences {
    var preference: ProxyPreferenceItem? = null
}

data class ProxyPreferenceItem(
    var proxyHost: String,
    var proxyPort: Int,
    var proxyMode: Proxy.Type
)