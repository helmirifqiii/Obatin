// settings.gradle.kts

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    // ⭐️ BLOK PLUGINS DENGAN SINTAKS KOTLIN DSL YANG BENAR ⭐️
    plugins {
        // Deklarasi Plugin Firebase Google Services
        id("com.google.gms.google-services").version("4.4.0") // Gunakan versi stabil terbaru

        // Deklarasi Plugin Kotlin (diperlukan untuk modul app)
        id("org.jetbrains.kotlin.android").version("1.9.22") // Sesuaikan versi Kotlin Anda
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ⭐️ TAMBAHAN KRITIS: REPOSITORY JITPACK UNTUK MPAndroidChart ⭐️
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Obatin"
include(":app")