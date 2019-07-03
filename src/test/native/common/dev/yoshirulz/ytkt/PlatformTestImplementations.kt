package dev.yoshirulz.ytkt

import kotlin.reflect.KClass
import kotlin.test.fail
import kotlin.test.assertFailsWith as stdlibAssertFailsWith
import kotlin.test.assertTrue as stdlibAssertTrue

actual fun <T: Throwable> assertFailsWith(exceptionClass: KClass<T>, block: suspend () -> Unit) {
	stdlibAssertFailsWith(exceptionClass) { nativeRunBlocking(block = block) }
}

actual fun assertSucceeds(block: suspend () -> Any?) {
	try {
		nativeRunBlocking(block = block)
	} catch (e: Exception) {
		fail(e.message)
	}
}

actual fun assertTrue(block: suspend () -> Boolean) = stdlibAssertTrue(nativeRunBlocking(block = block))

@Suppress("RedundantSuspendModifier", "UNREACHABLE_CODE")
actual suspend fun YTKtScraper.testCCsToNativeFile(metadata: CCTrackMetadata) = assertSucceeds {
	TODO()
//	val outputFilePath = File.create("/tmp/ytkt_tests/cctest") // Path.Combine(_tempDirPath, Guid.NewGuid().ToString())
//	Directory.CreateDirectory(_tempDirPath)
//	metadata.downloadCCsFromMetadata(outputFilePath)
//	val fileInfo = FileInfo(outputFilePath)
//	assertTrue(fileInfo.Exists)
//	assertTrue(fileInfo.Length > 0)
}

@Suppress("RedundantSuspendModifier", "UNREACHABLE_CODE")
actual suspend fun YTKtScraper.testStreamingToNativeFile(metadata: StreamMetadata) = assertSucceeds {
	TODO()
//	val outputFilePath = File.create("/tmp/ytkt_tests/streamtest") // Path.Combine(_tempDirPath, Guid.NewGuid().ToString())
//	Directory.CreateDirectory(_tempDirPath)
//	metadata.downloadStreamFromMetadata(outputFilePath)
//	val fileInfo = FileInfo(outputFilePath)
//	assertTrue(fileInfo.Exists)
//	assertEquals(metadata.size, fileInfo.Length)
}
