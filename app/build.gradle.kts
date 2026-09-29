import java.util.Properties

plugins {
    alias(libs.plugins.hisabkitab.android.application)
    alias(libs.plugins.hisabkitab.android.compose)
    alias(libs.plugins.hisabkitab.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

/**
 * Release signing is read from `keystore.properties` in the project root (never committed).
 * Without it, release builds are produced unsigned, so CI and fresh clones still build.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "com.hisabkitab"

    defaultConfig {
        applicationId = "com.hisabkitab.app"
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    androidResources {
        // Lists the bundled translations so Android 13+ offers them in system per-app language settings.
        generateLocaleConfig = true
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.transactions)
    implementation(projects.feature.stats)
    implementation(projects.feature.categories)
    implementation(projects.feature.budget)
    implementation(projects.feature.settings)

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.notifications)
    implementation(projects.core.security)
    implementation(projects.core.ui)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.serialization.json)

    baselineProfile(projects.baselineprofile)
}
