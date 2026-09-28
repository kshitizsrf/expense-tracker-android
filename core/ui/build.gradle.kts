// UI pieces shared by several features that depend on domain models.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.android.compose)
}

android {
    namespace = "com.hisabkitab.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    api(projects.core.common)
}
