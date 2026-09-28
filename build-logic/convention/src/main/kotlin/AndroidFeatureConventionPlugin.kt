import com.hisabkitab.buildlogic.bundle
import com.hisabkitab.buildlogic.libs
import com.hisabkitab.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.project

/**
 * A feature module: a Compose UI library with Hilt view models that depends only on `:core:*`
 * modules. Features never depend on each other; the app wires them together via navigation.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "hisabkitab.android.library")
        apply(plugin = "hisabkitab.android.compose")
        apply(plugin = "hisabkitab.hilt")

        dependencies.add("implementation", dependencies.project(":core:model"))
        dependencies.add("implementation", dependencies.project(":core:common"))
        dependencies.add("implementation", dependencies.project(":core:data"))
        dependencies.add("implementation", dependencies.project(":core:designsystem"))
        dependencies.add("implementation", dependencies.project(":core:ui"))

        dependencies.add("implementation", libs.bundle("lifecycle"))
        dependencies.add("implementation", libs.library("androidx-hilt-lifecycle-viewmodel-compose"))
    }
}
