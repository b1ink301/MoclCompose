@file:Suppress("unused")

package kr.b1ink.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

class AndroidComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        plugins.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure(ComposeCompilerGradlePluginExtension::class.java) {
            featureFlags.addAll(
                ComposeFeatureFlag.OptimizeNonSkippingGroups
            )

            includeSourceInformation.set(true)

            dependencies {
                val composeBom = platform(
                    libs.findLibrary("compose-bom")
                        .get()
                )
                "api"(composeBom)
                "testApi"(composeBom)
                "androidTestApi"(composeBom)

                "implementation"(
                    libs.findBundle("compose")
                        .get()
                )
                "debugImplementation"(
                    libs.findBundle("compose.debug")
                        .get()
                )
                "implementation"(
                    libs.findLibrary("kotlinx.serialization.json")
                        .get()
                )
            }
        }
    }
}