plugins {
    alias(libs.plugins.hisabkitab.android.feature)
}

android {
    namespace = "com.hisabkitab.feature.settings"
}

dependencies {
    implementation(projects.core.security)
}
