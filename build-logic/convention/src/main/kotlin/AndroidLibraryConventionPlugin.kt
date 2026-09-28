import com.android.build.api.dsl.LibraryExtension
import com.hisabkitab.buildlogic.JAVA_VERSION
import com.hisabkitab.buildlogic.addUnitTestDependencies
import com.hisabkitab.buildlogic.libs
import com.hisabkitab.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/** Base setup shared by every `:core:*` and `:feature:*` module. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(libs.version("compileSdk"))
            }
            defaultConfig {
                minSdk = libs.version("minSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
        }
        addUnitTestDependencies()
    }
}
