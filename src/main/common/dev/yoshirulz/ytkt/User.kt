package dev.yoshirulz.ytkt

data class User(val name: Username) {
	@EntryPoint
	val canonicalURI: URI get() = name.canonicalURI

	override fun toString() = "[user] $name"
}

data class Username internal constructor(val raw: String) {
	val canonicalURI: URI get() = ytCanonicalURI("/user/$raw")

	@RequiresXMLParser
	suspend fun getChannelID(scraper: YTKtScraper): ChannelID = ChannelID(scraper.getAndParseHTML(canonicalURI).querySelector("meta[property=\"og:url\"]")!!.getAttribute("content").substringAfter("channel/"))

	@Suppress("RedundantSuspendModifier")
	suspend fun getData(@Suppress("UNUSED_PARAMETER") scraper: YTKtScraper): User = User(this)

	override fun toString() = raw

	companion object {
		/** matches `dwangoAC` */
		private val USERNAME_PATTERN = Regex("^[0-9A-Za-z]{1,20}\$")

		fun parse(raw: String): Username? = if (USERNAME_PATTERN matches raw) Username(raw) else null

		fun parseFromURI(uri: URI): Username? = uri.encodedPath.let {
			if (YouTubeDomain.ofURI(uri) == YouTubeDomain.MainOrMirror && it.startsWith("/user/"))
				parse(it.substring(6).substringBefore('/'))
			else null
		}
	}
}
