// App lock: biometric / device-credential authentication and the lock screen.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.android.compose)
    alias(libs.plugins.hisabkitab.hilt)
}

android {
    namespace = "com.hisabkitab.core.security"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.process)
}
