package it.vfsfitvnm.innertube.utils

import java.net.Proxy

object ProxyPreferences {
    var preference: ProxyPreferenceItem? = null
}

data class ProxyPreferenceItem(
    var http_proxy_host: String,
    var http_proxy_port: Int,
    var proxyMode: Proxy.Type
)