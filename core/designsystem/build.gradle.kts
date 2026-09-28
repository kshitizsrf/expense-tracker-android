// Theme, colors, typography and reusable Compose components. Also holds shared strings.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.android.compose)
}

android {
    namespace = "com.hisabkitab.core.designsystem"
}

dependencies {
    api(projects.core.icons)
    implementation(projects.core.model)
    implementation(projects.core.common)
}
