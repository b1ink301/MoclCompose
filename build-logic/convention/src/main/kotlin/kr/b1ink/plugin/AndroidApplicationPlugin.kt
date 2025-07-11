@file:Suppress("unused")

package kr.b1ink.plugin

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

internal class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(plugins) {
            apply("com.android.application")
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid()

            defaultConfig {
                targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            }
        }

        dependencies {
            "implementation"(project(":data"))
        }
    }
}