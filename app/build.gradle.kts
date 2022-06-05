plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    signingConfigs {
        create("release") {
        }
    }
    compileSdk = 32

    defaultConfig {
        applicationId = "it.vfsfitvnm.vimusic"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "0.1.0"
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "Debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appName"] = "ViMusic"
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "1.8"
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(projects.composeRouting)
    implementation(projects.composeReordering)

    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)

    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.android.media)
    implementation(libs.bundles.media3)

    implementation(libs.datastore)

    implementation(libs.room)
    kapt(libs.room.compiler)

    implementation(libs.guava.coroutines)

    implementation(projects.youtubeMusic)
}
