package kr.b1ink.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KotlinSerializationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(plugins) {
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        dependencies {
            "implementation"(libs.findLibrary("kotlinx.serialization.json").get())
        }
    }
}