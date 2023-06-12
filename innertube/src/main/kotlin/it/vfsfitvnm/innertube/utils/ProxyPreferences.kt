package it.vfsfitvnm.innertube.utils

object ProxyPreferences {
    var preference: ProxyPreferenceItem? = null
}

data class ProxyPreferenceItem(
    var http_proxy_host: String,
    var http_proxy_port: Int
)