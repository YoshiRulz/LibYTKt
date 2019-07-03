package dev.yoshirulz.ytkt

actual val userAgentString = "curl/7.63.0 (${nativeUAFragment()})"

expect fun nativeUAFragment(): String
