buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build", "gradle", "7.3.0")
        classpath(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                arrayOf("reports", "metrics").forEach {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-P", "plugin:androidx.compose.compiler.plugins.kotlin:${it}Destination=${project.buildDir.absolutePath}/compose_metrics"
                    )
                }
            }
        }
    }
}
