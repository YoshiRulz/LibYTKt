package dev.yoshirulz.ytkt

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.*

actual fun nativeUAFragment() = memScoped {
	val osInfo = alloc<utsname>()
	uname(osInfo.ptr)
	"${osInfo.sysname.toKString()}/${osInfo.release.toKString()}; ${osInfo.machine.toKString()}"
}

actual fun platformTimeFromYMD(year: Int, month: Int, day: Int) = platformTimeNow() //TODO this isn't even close to correct

actual fun platformTimeNow() = memScoped {
	val osInfo = alloc<timespec>()
	clock_gettime(CLOCK_REALTIME, osInfo.ptr)
	Timestamp(1000L * osInfo.tv_sec + osInfo.tv_nsec / 1000000L)
}
