import com.hisabkitab.buildlogic.libs
import com.hisabkitab.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/** Hilt dependency injection with KSP. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.google.devtools.ksp")
        apply(plugin = "com.google.dagger.hilt.android")

        dependencies.add("implementation", libs.library("hilt-android"))
        dependencies.add("ksp", libs.library("hilt-compiler"))
    }
}
