pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "HisabKitab"

include(":app")

include(":core:common")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:icons")
include(":core:model")
include(":core:notifications")
include(":core:ui")

include(":feature:budget")
include(":feature:categories")
include(":feature:home")
include(":feature:settings")
include(":feature:stats")
include(":feature:transactions")
