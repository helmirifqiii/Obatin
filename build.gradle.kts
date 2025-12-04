// build.gradle.kts (Root Project Level) - FINAL

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // Definisi global, jika diperlukan
}

plugins {
    // ⭐️ PLUGIN STANDAR (Untuk Android dan Kotlin) ⭐️
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ⭐️ PLUGIN GOOGLE SERVICES (Diperlukan di level root) ⭐️
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// Catatan: Pastikan Anda telah menambahkan repository JitPack di file settings.gradle.kts Anda.