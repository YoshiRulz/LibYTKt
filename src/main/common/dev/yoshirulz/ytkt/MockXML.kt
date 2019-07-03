@file:Suppress("UNUSED_PARAMETER")

package dev.yoshirulz.ytkt

private fun noTODO(): Nothing = throw NotImplementedError()

class XAttribute(val name: XNSName, val value: String, val IsNamespaceDeclaration: Boolean = false)

class XElement(copy: XElement? = null) {
	private var name: XNSName = noTODO()

	val textContent: String = noTODO()
	val text: String = noTODO()

	private fun attributes(): Iterable<XAttribute> = noTODO()
	private fun descendantsAndSelf(): Iterable<XElement> = noTODO()
	private fun replaceAttributes(replacement: Collection<XAttribute>): Unit = noTODO()

	fun descendants(s: String): List<XElement> = noTODO()
	fun attribute(name: String): XAttribute = attributes().first { it.name.localName == name }
	fun getAttribute(name: String): String = attribute(name).value
	fun element(s: String): XElement? = noTODO()
	/** from [this SE answer](https://stackoverflow.com/a/1147012) */
	fun stripNamespaces(): XElement = XElement(this).also { clone ->
		clone.descendantsAndSelf().forEach { e ->
			e.name = XNamespace.None.getName(e.name.localName)
			e.replaceAttributes(e.attributes()
				.filterNot { it.IsNamespaceDeclaration || it.name.namespace == XNamespace.XML || it.name.namespace == XNamespace.XMLns }
				.map { XAttribute(XNamespace.None.getName(it.name.localName), it.value) }
			)
		}
	}

	companion object {
		@RequiresXMLParser
		fun parse(s: String): XElement = noTODO()
	}
}

enum class XNamespace {
	None, XML, XMLns;
	fun getName(s: String): XNSName = noTODO()
}

interface XNSName {
	val namespace: XNamespace
	val localName: String
}

interface HTMLDocument {
	val source: XElement
	fun querySelector(s: String): XElement?
}

object HTMLParser {
	@RequiresXMLParser
	fun parse(raw: String): HTMLDocument = noTODO()
}
