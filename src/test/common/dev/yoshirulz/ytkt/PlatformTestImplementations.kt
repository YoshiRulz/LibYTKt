package dev.yoshirulz.ytkt

import kotlin.reflect.KClass

expect fun <T: Throwable> assertFailsWith(exceptionClass: KClass<T>, block: suspend () -> Unit)

expect fun assertSucceeds(block: suspend () -> Any?)

expect fun assertTrue(block: suspend () -> Boolean)

expect fun <T> nativeRunBlocking(block: suspend () -> T): T

expect suspend fun YTKtScraper.testCCsToNativeFile(metadata: CCTrackMetadata)

expect suspend fun YTKtScraper.testStreamingToNativeFile(metadata: StreamMetadata)
