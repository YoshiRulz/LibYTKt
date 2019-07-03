package dev.yoshirulz.ytkt

import io.ktor.http.Url

const val EMPTY_STRING = ""

fun String.parseURI(): URI = when {
	this.isBlank() -> throw UnrecognisedURIException(this, "Blank URI")
	this.startsWith("https://") -> Url(this)
	this.startsWith("http://") -> throw UnsecuredURIException(this)
	this.contains("://") -> throw UnrecognisedURIException(this)
	else -> Url("https://${this}")
}

/** "path-encoded" referring to URIs such as `example.com/api/endpoint/keyA/valueA/keyB/valueB` */
fun String.setURIPathEncParam(key: String, value: String): String =
	Regex("""/${Regex.escape(key)}/([^/]+)(?:/|$)""").find(this)?.let { match ->
		this.replace(match.groupValues[0], value) // if the param was found, replace it
	} ?: "$this/$key/$value" // else, it wasn't present, so append it

fun String.stripNonDigit(): String = this.replace(Regex("\\D"), EMPTY_STRING)

fun String.swapCharsAt(firstIndex: Int, secondIndex: Int): String =
	if (firstIndex < secondIndex) "${this.substring(0, firstIndex)}${this[secondIndex]}${this.substring(firstIndex + 1, secondIndex)}${this[firstIndex]}${this.substring(secondIndex + 1)}"
	else this.swapCharsAt(secondIndex, firstIndex)
