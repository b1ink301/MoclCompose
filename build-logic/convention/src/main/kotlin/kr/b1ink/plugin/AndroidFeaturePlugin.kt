@file:Suppress("unused")

package kr.b1ink.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

internal class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply<AndroidLibraryPlugin>()
        apply<AndroidHiltPlugin>()
        with(plugins) {
            apply("androidx.navigation.safeargs.kotlin")
        }

        dependencies {
            "implementation"(project(":data"))
            "implementation"(project(":domain"))
//                "implementation"(project(":design-system"))
//                "implementation"(project(":core:core-ui"))

            "implementation"(libs.findLibrary("androidx.appcompat").get())
            "implementation"(libs.findLibrary("androidx.core.ktx").get())
            "implementation"(libs.findLibrary("androidx.constraintlayout").get())
            "implementation"(libs.findLibrary("navigation.fragment.ktx").get())
            "implementation"(libs.findLibrary("navigation.ui.ktx").get())

            "implementation"(libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
            "implementation"(libs.findLibrary("androidx.lifecycle.viewmodel.ktx").get())
            "implementation"(libs.findLibrary("androidx.activity.ktx").get())
            "implementation"(libs.findLibrary("androidx.fragment.ktx").get())
            "implementation"(libs.findLibrary("androidx.recyclerview").get())
            "implementation"(libs.findLibrary("google.material").get())

            "implementation"(libs.findLibrary("kotlinx.coroutines.android").get())
            "implementation"(libs.findLibrary("kotlinx.coroutines.core").get())

            "implementation"(libs.findLibrary("timber").get())
        }
    }
}