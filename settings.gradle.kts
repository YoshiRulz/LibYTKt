@file:Suppress("SpellCheckingInspection")

enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "libytkt"

pluginManagement {
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "kotlinx-serialization") useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
		}
	}
}
