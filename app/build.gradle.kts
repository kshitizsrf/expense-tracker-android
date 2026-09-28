plugins {
    alias(libs.plugins.hisabkitab.android.application)
    alias(libs.plugins.hisabkitab.android.compose)
    alias(libs.plugins.hisabkitab.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.hisabkitab"

    defaultConfig {
        // Kept from the original app so existing installs upgrade in place and keep their data.
        applicationId = "com.example.budget_planner"
        versionCode = 2
        versionName = "2.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.feature.home)
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
    implementation(projects.core.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.serialization.json)
}
