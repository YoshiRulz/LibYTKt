package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope

data class User(val name: Username) {
	/** The user home page. */
	@EntryPoint
	val canonicalURI: URI get() = name.canonicalURI

	override fun toString() = "[user] $name"
}

data class Username internal constructor(val raw: String) {
	/** The user home page. */
	val canonicalURI: URI get() = ytCanonicalURI("/user/$raw")

	suspend fun getChannelID(httpClient: HttpClient): ChannelID = coroutineScope {
		ChannelID(
			canonicalURI.requestPageProxied(httpClient, this)
				.receiveSingleOrElse { throw PageRequestFailureException() }
				.querySelector("""meta[property="og:url"]""")!!
				.getAttribute("content")
				.substringAfter("channel/")
		)
	}

	@Suppress("RedundantSuspendModifier")
	suspend fun getData(@Suppress("UNUSED_PARAMETER") httpClient: HttpClient): User = User(this)

	override fun toString() = raw

	companion object {
		/** matches `dwangoAC` */
		private val USERNAME_PATTERN = Regex("^[0-9A-Za-z]{1,20}\$")

		fun parse(raw: String): Username? = if (USERNAME_PATTERN matches raw) Username(raw) else null

		@EntryPoint
		fun parseFromURI(uri: String): Username? = parseFromURI(uri.parseURI())

		fun parseFromURI(uri: URI): Username? = uri.encodedPath.let {
			if (YouTubeDomain.ofURI(uri) == YouTubeDomain.MainOrMirror && it.startsWith("/user/"))
				parse(it.substring(6).substringBefore('/'))
			else null
		}
	}
}
