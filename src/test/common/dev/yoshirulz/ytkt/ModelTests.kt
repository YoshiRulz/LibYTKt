package dev.yoshirulz.ytkt

import dev.yoshirulz.ytkt.StructuredTestData.ChannelIDs
import dev.yoshirulz.ytkt.StructuredTestData.PlaylistIDs
import dev.yoshirulz.ytkt.StructuredTestData.URIs
import dev.yoshirulz.ytkt.StructuredTestData.Usernames
import dev.yoshirulz.ytkt.StructuredTestData.VideoIDs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@EntryPoint
@UseExperimental(ExperimentalTime::class, ExperimentalUnsignedTypes::class)
object ModelTests {
	private val mockCCs get() = ClosedCaptionsTrack(
		listOf(
			CCEntry("Hello", RelativeTimestamp(0L)..RelativeTimestamp(1000L)),
			CCEntry("world", RelativeTimestamp(2000L)..RelativeTimestamp(3000L))
		)
	)

	@Suppress("SpellCheckingInspection")
	private val mockChannel get() = Channel(
		ChannelID("UCTESTTESTTESTTESTTESTTE"),
		"Test Channel",
		"test"
	)

	@Suppress("SpellCheckingInspection")
	private val mockPlaylist get() = Playlist(
		PlaylistID("PLTESTTESTTESTTESTTESTTESTTESTTEST", PlaylistID.Companion.PlaylistType.Playlist),
		"Test Author",
		"Test Playlist",
		"test",
		1337UL,
		listOf(mockVideo)
	)

	private val mockVideo get() = Video(
		VideoID("-TEST-TEST-"),
		"Test Author",
		Timestamp.now(),
		"Test Video",
		"test",
		120.seconds,
		emptyList(),
		1337UL,
		13UL,
		37UL
	)

	@Test
	fun testChannelIDParsing() {
		ChannelIDs.valid.forEach { assertEquals(it, ChannelID.parse(it)?.raw) }
		ChannelIDs.invalid.forEach { assertNull(ChannelID.parse(it)) }
		URIs.correctProtocol.forEach { assertEquals(it.expectedCID, ChannelID.parseFromURI(it.uri)) }
	}

	@Test
	fun testChannelURIRoundTrip() = mockChannel.id.let { assertEquals(it, ChannelID.parseFromURI(it.canonicalURI)) }

	@Test
	fun testGetCCs() {
		assertEquals("Hello", mockCCs.captionAt(RelativeTimestamp(500L))?.text)
		assertNull(mockCCs.captionAt(RelativeTimestamp(5000L)), "received a CC when a nonexistent one was requested")
	}

	@Test
	fun testPlaylistIDParsing() {
		PlaylistIDs.valid.forEach { assertEquals(it, PlaylistID.parse(it)?.raw) }
		PlaylistIDs.invalid.forEach { assertNull(PlaylistID.parse(it)) }
		URIs.correctProtocol.forEach { assertEquals(it.expectedPID, PlaylistID.parseFromURI(it.uri)) }
	}

	@Test
	fun testPlaylistURIRoundTrips() = mockPlaylist.let { playlist ->
		assertEquals(playlist.id, PlaylistID.parseFromURI(playlist.id.canonicalURI))
		arrayOf(playlist.firstVidWatchURI, playlist.firstVidEmbedURI, playlist.firstVidShortURI).forEach { uri ->
			assertEquals(playlist.id, PlaylistID.parseFromURI(uri))
			assertEquals(playlist.videos[0].id, VideoID.parseFromURI(uri))
		}
	}

	@Test
	fun testProtocolAssertion() {
		URIs.nonWebProtocol.forEach {
			assertFailsWith(UnrecognisedURIException::class) { it.parseURI() }
		}
		URIs.unsecureProtocol.forEach {
			assertFailsWith(UnsecuredURIException::class) { it.parseURI() }
		}
	}

	@Test
	fun testRelativeTimestampToString() = mapOf(
		0L to "00:00:00.000",
		12345678L to "03:25:45.678",
		3511884680L to "975:31:24.680"
	).forEach { (l, expected) ->
		assertEquals(expected, RelativeTimestamp(l).toString())
	}

	@Test
	fun testUsernameParsing() {
		Usernames.valid.forEach { assertEquals(it, Username.parse(it)?.raw) }
		Usernames.invalid.forEach { assertNull(Username.parse(it)) }
		URIs.correctProtocol.forEach { assertEquals(it.expectedUsername, Username.parseFromURI(it.uri)) }
	}

	@Test
	fun testVideoIDParsing() {
		VideoIDs.valid.forEach { assertEquals(it, VideoID.parse(it)?.raw) }
		VideoIDs.invalid.forEach { assertNull(VideoID.parse(it)) }
		URIs.correctProtocol.forEach { assertEquals(it.expectedVID, VideoID.parseFromURI(it.uri)) }
	}

	@Test
	fun testVideoURIRoundTrips() = mockVideo.id.let { id ->
		arrayOf(id.canonicalURI, id.embedURI, id.shortURI).forEach { uri ->
			assertEquals(id, VideoID.parseFromURI(uri))
		}
	}
}
