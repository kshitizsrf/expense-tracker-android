plugins {
    alias(libs.plugins.hisabkitab.android.feature)
}

android {
    namespace = "com.hisabkitab.feature.onboarding"
}

dependencies {
    implementation(projects.core.security)
}
