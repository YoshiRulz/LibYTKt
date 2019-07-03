package dev.yoshirulz.ytkt

import kotlin.browser.window
import kotlin.js.Date
import kotlin.math.roundToLong

private fun Date.toTimestamp() = Timestamp(getTime().roundToLong())

actual val userAgentString = window.navigator.userAgent

actual fun platformTimeFromYMD(year: Int, month: Int, day: Int) = Date(year, month - 1, day).toTimestamp()

actual fun platformTimeNow() = Date(Date.now()).toTimestamp()
