plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.artifex.mupdf.fitz"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// This module hosts the MuPDF AAR built from mupdf-android-fitz.
// Place the AAR in libs/ and uncomment:
// dependencies {
//     api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
// }
//
// Or use the Maven artifact when available:
// dependencies {
//     api("com.artifex.mupdf:fitz:1.27.1")
// }
