package dev.yoshirulz.ytkt

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

@Suppress("NOTHING_TO_INLINE")
private inline fun noTODO(): Nothing = throw NotImplementedError()

interface HTMLPageElement {
    val textContent: String
    val text: String
    fun getAttribute(name: String): String
}

interface XMLPageElement {
    fun attribute(name: String): String
    fun descendants(s: String): List<XMLPageElement>
    fun element(s: String): XMLPageElement?

}

@Suppress("unused")
val HTMLPage.source: HTMLPageElement get() = noTODO()

@Suppress("unused")
val XMLPage.root: XMLPageElement get() = noTODO()

@Suppress("unused", "UNUSED_PARAMETER")
fun HTMLPage.querySelector(s: String): HTMLPageElement? = noTODO()

expect class HTMLPage

expect class XMLPage

expect fun requestPage(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage>

expect fun requestPageProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage>

expect fun requestPlaintext(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String>

expect fun requestPlaintextProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String>

expect fun requestXML(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage>

expect fun requestXMLProxied(uri: String, httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage>

expect fun URI.requestPage(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage>

expect fun URI.requestPageProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<HTMLPage>

expect fun URI.requestPlaintext(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String>

expect fun URI.requestPlaintextProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<String>

expect fun URI.requestXML(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage>

expect fun URI.requestXMLProxied(httpClient: HttpClient, scope: CoroutineScope): ReceiveChannel<XMLPage>
