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
            version("kotlin", "1.7.0")
            plugin("kotlin-serialization","org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            library("android-media", "androidx.media", "media").version("1.6.0")

            version("compose-compiler", "1.2.0")

            version("compose", "1.3.0-alpha01")
            library("compose-foundation", "androidx.compose.foundation", "foundation").versionRef("compose")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("compose")
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("compose")
            library("compose-ripple", "androidx.compose.material", "material-ripple").versionRef("compose")

            library("compose-shimmer", "com.valentinilk.shimmer", "compose-shimmer").version("1.0.3")

            library("compose-activity", "androidx.activity", "activity-compose").version("1.5.0-rc01")

            library("compose-coil", "io.coil-kt", "coil-compose").version("2.1.0")

            version("accompanist", "0.24.12-rc")
            library("accompanist-systemuicontroller", "com.google.accompanist", "accompanist-systemuicontroller").versionRef("accompanist")

            version("room", "2.5.0-alpha02")
            library("room", "androidx.room", "room-ktx").versionRef("room")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room")

            version("media3", "1.0.0-beta01")
            library("exoplayer", "androidx.media3", "media3-exoplayer").versionRef("media3")

            version("ktor", "2.0.2")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-client-encoding", "io.ktor", "ktor-client-encoding").versionRef("ktor")
            library("ktor-client-serialization", "io.ktor", "ktor-client-serialization").versionRef("ktor")
            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            library("brotli", "org.brotli", "dec").version("0.1.2")

            library("desugaring", "com.android.tools", "desugar_jdk_libs").version("1.1.5")
        }

        create("testLibs") {
            library("junit", "junit", "junit").version("4.13.2")
        }
    }
}

rootProject.name = "ViMusic"
include(":app")
include(":compose-routing")
include(":compose-reordering")
include(":youtube-music")
include(":ktor-client-brotli")
