// Room database: entities, DAOs, default data and the v1 importer.
plugins {
    alias(libs.plugins.hisabkitab.android.library)
    alias(libs.plugins.hisabkitab.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.hisabkitab.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
