package kr.b1ink.plugin

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(plugins) {
            apply("com.android.library")
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid()
        }

//        dependencies {
//            "implementation"(libs.findLibrary("junit4").get())
//        }
    }
}