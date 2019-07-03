package dev.yoshirulz.ytkt

import kotlin.reflect.KClass

val TestNotApplicable = Unit

actual fun <T: Throwable> assertFailsWith(exceptionClass: KClass<T>, block: suspend () -> Unit): Unit = TODO()

actual fun assertSucceeds(block: suspend () -> Any?): Unit = TODO()

actual fun assertTrue(block: suspend () -> Boolean): Unit = TODO()

actual fun <T> nativeRunBlocking(block: suspend () -> T): T = TODO()

@Suppress("RedundantSuspendModifier")
actual suspend fun YTKtScraper.testCCsToNativeFile(metadata: CCTrackMetadata) = TestNotApplicable

@Suppress("RedundantSuspendModifier")
actual suspend fun YTKtScraper.testStreamingToNativeFile(metadata: StreamMetadata) = TestNotApplicable
