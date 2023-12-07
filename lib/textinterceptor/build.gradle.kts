plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.okhttp)
}
