@file:Suppress("SpellCheckingInspection")

plugins {
	kotlin("multiplatform") version "1.3.50"
	id("kotlinx-serialization") version "1.3.50"
	`maven-publish`
}

group = "dev.yoshirulz"
version = "0.1.1"

repositories {
	jcenter()
	maven("https://kotlin.bintray.com/ktor")
}

kotlin {
	//TODO android
	js()
	linuxX64("linux")
	mingwX64("windoze")

	@Suppress("UNUSED_VARIABLE")
	sourceSets {
		fun depStr(partialCoords: String, version: String) = "$partialCoords:$version"

		fun kotlinx(module: String, version: String) = depStr("org.jetbrains.kotlinx:kotlinx-$module", version)

		fun coroutinesCore(module: String) = kotlinx("coroutines-core-$module", "1.3.1")

		fun ktorClient(module: String) = depStr("io.ktor:ktor-client-$module", "1.3.0-beta-1")

		all {
			languageSettings.enableLanguageFeature("InlineClasses")
			languageSettings.useExperimentalAnnotation("kotlin.Experimental")
		}

		val commonMain by getting {
			kotlin.srcDir("src/main/common")
			dependencies {
				api(coroutinesCore("common"))
				implementation(kotlin("stdlib-common"))
				implementation(kotlinx("serialization-runtime", "0.13.0"))
				implementation(ktorClient("core"))
				implementation(ktorClient("json"))
				implementation(ktorClient("serialization"))
			}
		}
		val commonTest by getting {
			kotlin.srcDir("src/test/common")
			dependencies {
				api(ktorClient("mock"))
				implementation(kotlin("test-annotations-common"))
				implementation(kotlin("test-common"))
			}
		}

		val jsMain by getting {
			kotlin.srcDir("src/main/js")
			dependsOn(commonMain)
			dependencies {
				api(coroutinesCore("js"))
				implementation(kotlin("stdlib-js"))
				implementation(ktorClient("js"))
				implementation(ktorClient("json-js"))
				implementation(ktorClient("serialization-js"))
			}
		}
		val jsTest by getting {
			kotlin.srcDir("src/test/js")
			dependsOn(commonTest)
			dependencies {
				api(ktorClient("mock-js"))
				implementation(kotlin("test-js"))
			}
		}

		val nativeCommonMain by creating {
			kotlin.srcDir("src/main/native/common")
			dependsOn(commonMain)
			dependencies {
				api(coroutinesCore("native"))
				implementation(ktorClient("core-native"))
				implementation(ktorClient("curl"))
				implementation(ktorClient("json-native"))
				implementation(ktorClient("serialization-native"))
			}
		}
		val nativeCommonTest by creating {
			kotlin.srcDir("src/test/native/common")
			dependsOn(commonTest)
			dependencies {
				api(ktorClient("mock-native"))
			}
		}

		val linuxMain by getting {
			kotlin.srcDir("src/main/native/linux")
			dependsOn(nativeCommonMain)
		}
		val linuxTest by getting {
			kotlin.srcDir("src/test/native/linux")
			dependsOn(nativeCommonTest)
		}

		val windozeMain by getting {
			kotlin.srcDir("src/main/native/windoze")
			dependsOn(nativeCommonMain)
		}
		val windozeTest by getting {
			kotlin.srcDir("src/test/native/windoze")
			dependsOn(nativeCommonTest)
		}
	}
}
