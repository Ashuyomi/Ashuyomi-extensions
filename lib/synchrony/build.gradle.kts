plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = AndroidConfig.compileSdk
    namespace = "eu.kanade.tachiyomi.lib.synchrony"

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.bundles.common)
}
