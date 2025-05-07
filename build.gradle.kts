plugins {
    id("com.android.library") version "8.10.0" // Updated to the latest version
    id("org.jetbrains.kotlin.android") version "1.9.22" // Ensure compatibility
}

android {
    namespace = "tools.quanta.sdk"
    compileSdk = 36 // Or your desired compile SDK version

    defaultConfig {
        minSdk = 21 // Or your desired min SDK version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion =
                "1.5.8" // Ensure this is compatible with your Kotlin version (1.9.22)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22") // Explicitly add kotlin-stdlib
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("androidx.compose.runtime:runtime:1.6.7")
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0") // Added coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Added coroutines android
    implementation("androidx.fragment:fragment-ktx:1.6.1") // Add AndroidX Fragment library
}