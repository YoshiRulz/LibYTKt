@file:Suppress("unused", "UNUSED_PARAMETER")

package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import io.ktor.client.features.UserAgent
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel

typealias URI = Url

internal enum class YouTubeDomain {
	Invalid, MainOrMirror, Short;

	companion object {
		const val CANONICAL_DOMAIN = "www.youtube.com"

		const val SHORT_DOMAIN = "youtu.be"

		private val MIRRORS = arrayOf("youtube.com", "youtube.de")

		fun ofURI(uri: URI) = when {
			uri.host == SHORT_DOMAIN -> Short
			uri.host.removePrefix("www.") in MIRRORS -> MainOrMirror
			else -> Invalid
		}
	}
}

const val ytktVersion = "0.1.1"

internal fun genDefaultHTTPClient() = HttpClient {
	install(JsonFeature)
	install(UserAgent) {
		agent = "$userAgentString YTKt/$ytktVersion"
	}
}

internal fun ytCanonicalURI(path: String, parameters: Parameters = Parameters.Empty, isShortened: Boolean = false) = URI(
	URLProtocol.HTTPS,
	if (isShortened) YouTubeDomain.SHORT_DOMAIN else YouTubeDomain.CANONICAL_DOMAIN,
	0,
	path,
	parameters,
	EMPTY_STRING,
	null,
	null,
	false
)

@Suppress("RedundantSuspendModifier")
@UseExperimental(ExperimentalUnsignedTypes::class)
internal suspend fun HttpClient.getContentLength(requestUri: URI) = 0UL as ULong? //TODO return content length from GET (HEADER?) response headers, null if response status code isn't 2xx

@UseExperimental(ExperimentalUnsignedTypes::class)
internal fun HttpClient.stream(metadata: StreamMetadata) = metadata.uri.toString().let { uriString ->
	MediaStream(uriString, 0UL until metadata.size, uriString.indexOf("ratebypass").let { it != -1 && uriString.substring(it + 11..it + 13) == "yes" })
}

internal suspend fun <E> ReceiveChannel<E>.receiveSingleOrElse(defaultValue: () -> E) = try {
	this.receive()
} catch (_: ClosedReceiveChannelException) {
	defaultValue()
} finally {
	this.cancel()
}

suspend fun <C: Closeable, R> C.useThis(block: suspend C.() -> R): R = block(this).also { close() }

/** Creates a new instance of [YTKtScraper] using [httpClient], passes it to the block, and ensures it closes correctly. That means [httpClient] will be closed, so if you need it open don't use this function. */
suspend fun <T> withConfiguredScraper(httpClient: HttpClient, block: suspend YTKtScraper.() -> T): T = YTKtScraper(httpClient).useThis(block)

/** Creates a new instance of [YTKtScraper], passes it to the block, and ensures it closes correctly. */
suspend fun <T> withScraper(block: suspend YTKtScraper.() -> T): T = YTKtScraper(genDefaultHTTPClient()).useThis(block)

fun Parameters.copyAndSet(key: String, value: String): Parameters =
	if (this[key] == null) this + parametersOf(key, value)
	else parametersOf(
		*this.entries()
			.mapTo(mutableListOf()) { it.toPair() }
			.also {
				it.remove(it.first { (k, _) -> k == key })
				it.add(Pair(key, listOf(value)))
			}
			.toTypedArray()
	)

fun URI.copyAndSetParam(key: String, value: String): URI = this.copy(parameters = this.parameters.copyAndSet(key, value))

expect val userAgentString: String
