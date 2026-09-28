package com.hisabkitab.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()

internal fun VersionCatalog.bundle(alias: String) = findBundle(alias).get()

internal val JAVA_VERSION = JavaVersion.VERSION_17

/** Test dependencies every module gets. */
internal fun Project.addUnitTestDependencies() {
    dependencies.add("testImplementation", libs.library("junit"))
    dependencies.add("testImplementation", libs.library("kotlinx-coroutines-test"))
}
