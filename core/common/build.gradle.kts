// Pure logic shared across layers: money, calculator, time helpers and DI qualifiers.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.hilt)
}

android {
    namespace = "com.hisabkitab.core.common"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.android)
}
