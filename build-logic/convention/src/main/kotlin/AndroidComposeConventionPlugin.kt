import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.hisabkitab.buildlogic.bundle
import com.hisabkitab.buildlogic.libs
import com.hisabkitab.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType

/** Enables Jetpack Compose. Apply after the application or library convention plugin. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        extensions.findByType<ApplicationExtension>()?.buildFeatures?.compose = true
        extensions.findByType<LibraryExtension>()?.buildFeatures?.compose = true

        val bom = dependencies.platform(libs.library("androidx-compose-bom"))
        dependencies.add("implementation", bom)
        dependencies.add("implementation", libs.bundle("compose"))
        dependencies.add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
    }
}
