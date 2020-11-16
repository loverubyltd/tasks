import com.diffplug.gradle.spotless.SpotlessExtension

buildscript {
    repositories {
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}")
    }
}


plugins {
    id("com.diffplug.spotless") version "5.7.0"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
    }

    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        ratchetFrom = "origin/main" // only format files which have changed since origin/main

        kotlin {
            target(fileTree(".") {
                include("**/*.kt")
                exclude("**/.gradle/**")
                exclude("**/build/**")
            })
            ktlint()
        }
        kotlinGradle {
            target(
                "*.gradle.kts", "**/*.gradle.kts"
            )
            ktlint()
        }
    }

  apply(plugin = "io.gitlab.arturbosch.detekt")

  detekt {
      toolVersion = "1.14.2"
      config = files("config/detekt/detekt.yml")
      buildUponDefaultConfig = true
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "6.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
