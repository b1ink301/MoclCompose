package kr.b1ink.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidTestPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            apply<JUnit5Plugin>()

            // Application 또는 Library extension에서 테스트 설정
            extensions.findByType(ApplicationExtension::class.java)?.apply {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
                }
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
                packaging {
                    resources.excludes.add("META-INF/LICENSE*")
                }
            }

            extensions.findByType(LibraryExtension::class.java)?.apply {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
                }
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
                packaging {
                    resources.excludes.add("META-INF/LICENSE*")
                }
            }

            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
                "debugImplementation"(libs.findLibrary("truth").get())
                "testImplementation"(libs.findLibrary("robolectric").get())
                "androidTestImplementation"(libs.findBundle("androidx.android.test").get())
            }
        }
}
