// Repositories (the single source of truth for features), preferences and CSV export.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.hilt)
}

android {
    namespace = "com.hisabkitab.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
}
