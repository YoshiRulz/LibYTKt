package dev.yoshirulz.ytkt

import kotlinx.coroutines.runBlocking

actual fun <T> nativeRunBlocking(block: suspend () -> T) = runBlocking { block() }
