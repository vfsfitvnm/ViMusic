plugins {
    kotlin("jvm")
}

sourceSets.all {
    java.srcDir("src/$name/kotlin")
}

dependencies {
    implementation(libs.kotlin.coroutines)
    testImplementation(testLibs.junit)
}
