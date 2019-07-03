package dev.yoshirulz.ytkt

/** Thrown when certain essential information can't be extracted, usually because YouTube changed some internals somewhere */
class UnrecognisedStructureException(failedOn: String): Exception("Could not find signature decipherer function $failedOn.")

/** Thrown by [parseURI] when the receiver is a blank string or specifies a protocol that is not HTTP(S) */
class UnrecognisedURIException(uri: String, message: String = "Unknown URI protocol: $uri"): URIParseException(uri, message)

/** Thrown by [parseURI] when the receiver specifies HTTP */
class UnsecuredURIException(uri: String): URIParseException(uri, "Use HTTPS: $uri")

open class URIParseException(val uri: String, message: String): Exception(message)

/** Thrown when a video is not playable because it requires purchase */
class VideoRequiresPurchaseException(id: VideoID, @Suppress("CanBeParameter") val previewVideoID: String): VideoUnplayableException(id, "Video [$id] is unplayable because it requires purchase (the response included a preview video at ID $previewVideoID).")

/**
 * Thrown when a video is not available and cannot be processed.
 * This can happen because the video does not exist, is deleted, is private, or due to other reasons.
 */
class VideoUnavailableException(id: VideoID): VideoUnplayableException(id, "The video is deleted, set to private, or otherwise unavailable.")

/**
 * Thrown when a video is not playable and its streams cannot be resolved.
 * This can happen because the video requires purchase, is blocked in your country, is controversial, or due to other reasons.
 */
open class VideoUnplayableException(val id: VideoID, reason: String? = null): Exception("Video [$id] is unplayable. Reason: ${reason ?: "none"}")
