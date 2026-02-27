import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
}

fun fetchGitCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--verify", "--short", "HEAD")
        .redirectErrorStream(true)
        .start()
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

val gitCommitHash = fetchGitCommitHash()

val keyProps = Properties()
val keyPropsFile: File = rootProject.file("signature/keystore.properties")
if (keyPropsFile.exists()) {
    keyProps.load(FileInputStream(keyPropsFile))
}

android {
    namespace = "org.openpdf.reader"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.openpdf.reader"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions.add("channel")
    productFlavors {
        create("github") {
            dimension = "channel"
        }
        create("fdroid") {
            dimension = "channel"
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = keyProps["keyAlias"] as String?
            keyPassword = keyProps["keyPassword"] as String?
            storeFile = keyProps["storeFile"]?.let { file(it as String) }
            storePassword = keyProps["storePassword"] as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "OpenPDF-${defaultConfig.versionName}-${gitCommitHash}.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    // Modules
    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-security"))
    implementation(project(":mupdf:mupdf-wrapper"))
    implementation(project(":feature:feature-viewer"))
    implementation(project(":feature:feature-annotations"))
    implementation(project(":feature:feature-forms"))
    implementation(project(":feature:feature-signatures"))
    implementation(project(":feature:feature-search"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-filemanager"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.profileinstaller)
    implementation(libs.material)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // AboutLibraries
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
