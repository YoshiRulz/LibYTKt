@file:Suppress("SpellCheckingInspection")

package dev.yoshirulz.ytkt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

// All these data class' names were guessed based on their usages. This is not an official API.

internal interface ParseableFromJSON<T> {
	val deserialiser: KSerializer<T>

	fun parse(rawJSON: String): T = json.parse(deserialiser, rawJSON)

	companion object {
		@UseExperimental(UnstableDefault::class)
		private val json = Json(JsonConfiguration.Default)
	}
}

@Serializable
data class YTAdaptiveStreamFormat(
	val itag: Int,
	val url: String,
	val mimeType: String,
	val bitrate: Int,
	val width: Int? = null,
	val height: Int? = null,
	val initRange: YTRange,
	val indexRange: YTRange,
	val lastModified: String,
	val contentLength: String,
	val quality: String,
	val fps: Int? = null,
	val qualityLabel: String? = null,
	val projectionType: String,
	val averageBitrate: Int,
	val colorInfo: YTColourInfo? = null,
	val highReplication: Boolean? = null,
	val audioQuality: String? = null,
	val approxDurationMs: String,
	val audioSampleRate: String? = null,
	val audioChannels: Int? = null
)

@Serializable
data class YTAdSafetyReason(
	val apmUserPreference: YTAPMUserPreference,
	val isWatchHistoryPaused: Boolean? = null,
	val isRelevantAdsOptout: Boolean? = null,
	val isEmbed: Boolean,
	val isFocEnabled: Boolean? = null,
	val isSearchHistoryPaused: Boolean? = null
)

@Serializable
data class YTAnnotationDetails(val playerAnnotationsUrlsRenderer: YTPlayerAnnotationsUrlsRenderer)

@Serializable
data class YTAPMUserPreference(val dummy: Unit = Unit)

@Serializable
data class YTCCAudioTrack(val captionTrackIndices: List<Int>, val defaultCaptionTrackIndex: Int, val visibility: String, val hasDefaultTrack: Boolean)

@Serializable
data class YTCCLanguage(val languageCode: String, val languageName: YTRichText)

@Serializable
data class YTCCTrack(
	val baseUrl: String,
	val name: YTRichText,
	val vssId: String,
	val languageCode: String,
	val rtl: Boolean? = null,
	val isTranslatable: Boolean,
	val kind: String? = null
)

@Serializable
data class YTCCTrackDetails(
	val captionTracks: List<YTCCTrack>? = null,
	val audioTracks: List<YTCCAudioTrack>? = null,
	val translationLanguages: List<YTCCLanguage>? = null,
	val defaultAudioTrackIndex: Int? = null
)

@Serializable
data class YTCCTrackDetailsContainer(val playerCaptionsTracklistRenderer: YTCCTrackDetails)

@Serializable
data class YTColourInfo(val primaries: String, val transferCharacteristics: String, val matrixCoefficients: String)

@Serializable
data class YTEmbedPagePlayerArgs(
	val vss_host: String,
	val enablecastapi: String,
	val adformat: String? = null, //TODO only seen as null, String is a guess
	val el: String,
	val cver: String,
	val cbrver: String,
	val view_count: Long,
	val origin: String,
	val authuser: Byte,
	val gapi_hint_params: String,
	val profile_picture: String,
	val channel_path: String,
	val user_display_name: String,
	val rel: String,
	val is_html5_mobile_device: Boolean,
	val hl: String,
	val fexp: String,
	val video_id: String,
	val c: String,
	val expanded_subtitle: String,
	val cbr: String,
	val innertube_api_version: String,
	val user_display_image: String,
	val is_embed: String,
	val length_seconds: Long,
	val subtitle: String,
	val embedded_player_response: String,
	val cr: String,
	val ssl: String,
	val allow_embed: Byte,
	val avg_rating: Double,
	val title: String,
	val cos: String,
	val innertube_context_client_version: String,
	val host_language: String,
	val embed_config: String,
	val expanded_title: String,
	val fflags: String,
	val allow_ratings: Byte,
	val eventid: String,
	val enablejsapi: String,
	val showwatchlater: String,
	val short_view_count_text: String,
	val innertube_api_key: String
)

@Serializable
data class YTEmbedPagePlayerConfig(val attrs: YTVideoPlayerAttrs, val sts: Long, val args: YTEmbedPagePlayerArgs, val assets: YTVideoPlayerAssets) {
	companion object: ParseableFromJSON<YTEmbedPagePlayerConfig> {
		override val deserialiser = serializer()
	}
}

@Serializable
data class YTIconDetails(val iconType: String)

@Serializable
data class YTMealbarPromoRendererBrowseEndpoint(val browseId: String, val params: String)

@Serializable
data class YTMealbarPromoRendererButtonDetails(val buttonRenderer: YTMealbarPromoRendererButtonRendererDetails)

@Serializable
data class YTMealbarPromoRendererButtonRendererDetails(
	val style: String,
	val size: String,
	val text: YTMealbarPromoRendererTextContainer,
	val navigationEndpoint: YTMealbarPromoRendererEndpointDetails? = null,
	val serviceEndpoint: YTMealbarPromoRendererEndpointDetails? = null,
	val trackingParams: String
)

@Serializable
data class YTMealbarPromoRendererDetails(
	val messageTexts: List<YTMealbarPromoRendererTextContainer>,
	val actionButton: YTMealbarPromoRendererButtonDetails,
	val dismissButton: YTMealbarPromoRendererButtonDetails,
	val triggerCondition: String,
	val style: String,
	val trackingParams: String,
	val impressionEndpoints: List<YTMealbarPromoRendererEndpointDetails>,
	val isVisible: Boolean,
	val messageTitle: YTMealbarPromoRendererTextContainer
)

@Serializable
data class YTMealbarPromoRendererEndpointDetails(val clickTrackingParams: String, val browseEndpoint: YTMealbarPromoRendererBrowseEndpoint? = null, val feedbackEndpoint: YTMealbarPromoRendererFeedbackEndpoint? = null)

@Serializable
data class YTMealbarPromoRendererFeedbackEndpoint(val feedbackToken: String, val uiActions: YTMealbarPromoRendererUIActions)

@Serializable
data class YTMealbarPromoRendererRunText(val text: String)

@Serializable
data class YTMealbarPromoRendererTextContainer(val runs: List<YTMealbarPromoRendererRunText>)

@Serializable
data class YTMealbarPromoRendererUIActions(val hideEnclosingContainer: Boolean)

@Serializable
data class YTPlaybackTrackingDetails(
	val videostatsPlaybackUrl: YTPlaybackTrackingURI,
	val videostatsDelayplayUrl: YTPlaybackTrackingURI,
	val videostatsWatchtimeUrl: YTPlaybackTrackingURI,
	val ptrackingUrl: YTPlaybackTrackingURI,
	val qoeUrl: YTPlaybackTrackingURI,
	val setAwesomeUrl: YTPlaybackTrackingURI,
	val atrUrl: YTPlaybackTrackingURI,
	val youtubeRemarketingUrl: YTPlaybackTrackingURI
)

@Serializable
data class YTPlaybackTrackingURI(val baseUrl: String, val elapsedMediaTimeSeconds: Int? = null)

@Serializable
data class YTPlayerAdParams(val enabledEngageTypes: String)

@Serializable
data class YTPlayerAdDetails(val playerLegacyDesktopWatchAdsRenderer: YTPlayerLegacyDesktopWatchAdsRendererDetails)

@Serializable
data class YTPlayerAnnotationsUrlsRenderer(val invideoUrl: String, val loadPolicy: String, val allowInPlaceSwitch: Boolean)

@Serializable
data class YTPlayerAttestationDetails(val playerAttestationRenderer: YTPlayerAttestationRenderer)

@Serializable
data class YTPlayerAttestationRenderer(val challenge: String)

@Serializable
data class YTPlayerAudioConfig(val loudnessDb: Double, val perceptualLoudnessDb: Double)

@Serializable
data class YTPlayerConfigDetails(val audioConfig: YTPlayerAudioConfig, val streamSelectionConfig: YTPlayerSelectionConfig, val mediaCommonConfig: YTPlayerMediaConfig)

@Serializable
data class YTPlayerEndscreenDetails(val endscreenUrlRenderer: YTPlayerEndscreenUrlRendererDetails)

@Serializable
data class YTPlayerEndscreenUrlRendererDetails(val url: String)

@Serializable
data class YTPlayerLegacyDesktopWatchAdsRendererDetails(val playerAdParams: YTPlayerAdParams)

@Serializable
data class YTPlayerMediaConfig(val dynamicReadaheadConfig: YTPlayerReadaheadConfig)

@Serializable
data class YTPlayerReadaheadConfig(val maxReadAheadMediaTimeMs: Int, val minReadAheadMediaTimeMs: Int, val readAheadGrowthRateMs: Int)

@Serializable
data class YTPlayerResponse(
	val streamingData: YTPlayerResponseStreamingData? = null,
	val playabilityStatus: YTPlayerResponsePlayabilityStatus? = null,
	val playerAds: List<YTPlayerAdDetails>? = null,
	val playbackTracking: YTPlaybackTrackingDetails? = null,
	val captions: YTCCTrackDetailsContainer? = null,
	val videoDetails: YTPlayerResponseVideoDetails? = null,
	val annotations: List<YTAnnotationDetails>? = null,
	val playerConfig: YTPlayerConfigDetails? = null,
	val storyboards: YTPlayerStoryboardDetails? = null,
	val trackingParams: String? = null,
	val attestation: YTPlayerAttestationDetails? = null,
	val messages: List<YTPlayerResponseMessage>? = null,
	val endscreen: YTPlayerEndscreenDetails? = null,
	val adSafetyReason: YTAdSafetyReason? = null
) {
	companion object: ParseableFromJSON<YTPlayerResponse> {
		override val deserialiser = serializer()
	}
}

@Serializable
data class YTPlayerResponseErrorMessageDetails(val subreason: YTRichText, val reason: YTRichText, val thumbnail: YTThumbnailList, val icon: YTIconDetails)

@Serializable
data class YTPlayerResponseErrorScreenDetails(
	val playerLegacyDesktopYpcTrailerRenderer: YTPlayerResponseLegacyPremiumTrailerDetails? = null,
	val ypcTrailerRenderer: YTPlayerResponsePremiumTrailerDetails? = null,
	val playerErrorMessageRenderer: YTPlayerResponseErrorMessageDetails? = null
)

@Serializable
data class YTPlayerResponseLegacyPremiumTrailerDetails(val trailerVideoId: String)

@Serializable
data class YTPlayerResponseMessage(val mealbarPromoRenderer: YTMealbarPromoRendererDetails)

@Serializable
data class YTPlayerResponsePlayabilityStatus(val playableInEmbed: Boolean? = null, val status: String? = null, val reason: String? = null, val errorScreen: YTPlayerResponseErrorScreenDetails? = null)

@Serializable
data class YTPlayerResponsePremiumTrailerDetails(val playerVars: String)

@Serializable
data class YTPlayerResponseStreamingData(
	val expiresInSeconds: Int,
	val formats: List<YTStreamFormat>? = null,
	val adaptiveFormats: List<YTAdaptiveStreamFormat>? = null,
	val dashManifestUrl: String? = null,
	val hlsManifestUrl: String? = null
)

@Serializable
data class YTPlayerResponseVideoDetails(
	val author: String,
	val viewCount: Long? = null,
	val title: String,
	val shortDescription: String,
	val isLive: Boolean? = null,
	val lengthSeconds: Int,
	val keywords: List<String>? = null,
	val channelId: String,
	val videoId: String? = null,
	val isOwnerViewing: Boolean? = null,
	val isCrawlable: Boolean? = null,
	val thumbnail: YTThumbnailList? = null,
	val useCipher: Boolean? = null,
	val averageRating: Double? = null,
	val allowRatings: Boolean? = null,
	val isPrivate: Boolean? = null,
	val isUnpluggedCorpus: Boolean? = null,
	val isLiveContent: Boolean? = null
)

@Serializable
data class YTPlayerSelectionConfig(val maxBitrate: String)

@Serializable
data class YTPlayerStoryboardDetails(val playerStoryboardSpecRenderer: YTPlayerStoryboardSpecRenderer)

@Serializable
data class YTPlayerStoryboardSpecRenderer(val spec: String)

@Serializable
data class YTPlaylistDetails(
	val title: String,
	val description: String? = null,
	val views: Long? = null, // system playlists have no views
	val video: List<YTVideoDetails>? = null,
	val author: String? = null // system playlists have no author
) {
	companion object: ParseableFromJSON<YTPlaylistDetails> {
		override val deserialiser = serializer()
	}
}

@Serializable
data class YTRange(val start: Int, val end: Int)

@Serializable
data class YTRichText(val simpleText: String)

@Serializable
data class YTSearchResults(val hits: Long, val video: List<YTVideoDetails>) {
	companion object: ParseableFromJSON<YTSearchResults> {
		override val deserialiser = serializer()
	}
}

@Serializable
data class YTStreamFormat(
	val itag: Int,
	val url: String,
	val mimeType: String,
	val bitrate: Int,
	val width: Int,
	val height: Int,
	val lastModified: String,
	val contentLength: String? = null,
	val quality: String,
	val qualityLabel: String,
	val projectionType: String,
	val averageBitrate: Int? = null,
	val audioQuality: String,
	val approxDurationMs: String? = null,
	val audioSampleRate: String? = null,
	val audioChannels: Int? = null
)

@Serializable
data class YTThumbnailDetails(val url: String, val width: Int, val height: Int)

@Serializable
data class YTThumbnailList(val thumbnails: List<YTThumbnailDetails>)

@Serializable
data class YTVideoDetails(
	val privacy: String,
	val user_id: String,
	val duration: String,
	val cc_license: Boolean,
	val rating: Float,
	val thumbnail: String,
	val added: String,
	val time_created: Long,
	val description: String,
	val dislikes: Long,
	val title: String,
	val author: String,
	val is_hd: Boolean,
	val comments: String,
	val keywords: String,
	val length_seconds: Int,
	val views: String,
	val is_cc: Boolean,
	/** YT video IDs are encrypted? Or were, and the JSON key remains for backwards compatibility? */ val encrypted_id: String,
	val category_id: Byte,
	val likes: Long,
	val session_data: String? = null,
	val endscreen_autoplay_session_data: String? = null
)

@Serializable
data class YTVideoPlayerAssets(val js: String, val css: String)

@Serializable
data class YTVideoPlayerAttrs(val id: String, val width: String? = null, val height: String? = null)

@Serializable
data class YTWatchPagePlayerArgs(
	val cbrver: String,
	val innertube_api_version: String,
	val video_id: String,
	val host_language: String,
	val csi_page_type: String,
	val enablecsi: String,
	val ssl: String,
	val cr: String,
	val loaderUrl: String,
	val fflags: String,
	val hl: String,
	val cbr: String,
	val fexp: String,
	val title: String,
	val enablejsapi: String,
	val show_content_thumbnail: Boolean,
	val enabled_engage_types: String,
	val c: String,
	val fmt_list: String,
	val player_response: String,
	val ucid: String,
	val length_seconds: String,
	val url_encoded_fmt_stream_map: String,
	val gapi_hint_params: String,
	val adaptive_fmts: String,
	val cver: String,
	val innertube_context_client_version: String,
	val account_playback_token: String,
	val timestamp: String,
	val watermark: String,
	val innertube_api_key: String,
	val cos: String,
	val vss_host: String,
	val author: String
)

@Serializable
data class YTWatchPagePlayerConfig(val attrs: YTVideoPlayerAttrs, val sts: Long, val args: YTWatchPagePlayerArgs, val assets: YTVideoPlayerAssets) {
	companion object: ParseableFromJSON<YTWatchPagePlayerConfig> {
		override val deserialiser = serializer()
	}
}
