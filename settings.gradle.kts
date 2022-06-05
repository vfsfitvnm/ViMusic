enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.21")
            alias("kotlin-serialization").toPluginId("org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            alias("android-media").to("androidx.media", "media").version("1.6.0")

            version("compose", "1.2.0-beta03")
            alias("compose-foundation").to("androidx.compose.foundation", "foundation").versionRef("compose")
            alias("compose-ui").to("androidx.compose.ui", "ui").versionRef("compose")
            alias("compose-ui-util").to("androidx.compose.ui", "ui-util").versionRef("compose")
            alias("compose-ripple").to("androidx.compose.material", "material-ripple").versionRef("compose")

            alias("compose-shimmer").to("com.valentinilk.shimmer", "compose-shimmer").version("1.0.3")

            alias("compose-activity").to("androidx.activity", "activity-compose").version("1.5.0-rc01")

            alias("compose-coil").to("io.coil-kt", "coil-compose").version("2.1.0")

            version("accompanist", "0.24.10-beta")
            alias("accompanist-systemuicontroller").to("com.google.accompanist", "accompanist-systemuicontroller").versionRef("accompanist")
            alias("accompanist-flowlayout").to("com.google.accompanist", "accompanist-flowlayout").versionRef("accompanist")

            version("room", "2.5.0-alpha01")
            alias("room").to("androidx.room", "room-ktx").versionRef("room")
            alias("room-compiler").to("androidx.room", "room-compiler").versionRef("room")

            version("media3", "1.0.0-alpha03")
            alias("media3-ui").to("androidx.media3", "media3-ui").versionRef("media3")
            alias("media3-session").to("androidx.media3", "media3-session").versionRef("media3")
            alias("media3-exoplayer").to("androidx.media3", "media3-exoplayer").versionRef("media3")
            bundle("media3", listOf("media3-ui", "media3-session", "media3-exoplayer"))

            version("ktor", "2.0.2")
            alias("ktor-client-core").to("io.ktor", "ktor-client-core").versionRef("ktor")
            alias("ktor-client-cio").to("io.ktor", "ktor-client-cio").versionRef("ktor")
            alias("ktor-client-content-negotiation").to("io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            alias("ktor-client-encoding").to("io.ktor", "ktor-client-encoding").versionRef("ktor")
            alias("ktor-client-serialization").to("io.ktor", "ktor-client-serialization").versionRef("ktor")
            alias("ktor-serialization-json").to("io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            alias("brotli").to("org.brotli", "dec").version("0.1.2")

            alias("guava-coroutines").to("org.jetbrains.kotlinx", "kotlinx-coroutines-guava").version("1.6.2")
        }

        create("testLibs") {
            alias("junit").to("junit", "junit").version("4.13.2")
        }
    }
}

rootProject.name = "ViMusic"
include(":app")
include(":compose-routing")
include(":compose-reordering")
include(":youtube-music")
include(":ktor-client-brotli")
