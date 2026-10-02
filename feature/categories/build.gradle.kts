plugins {
    alias(libs.plugins.hisabkitab.android.feature)
}

android {
    namespace = "com.hisabkitab.feature.categories"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
