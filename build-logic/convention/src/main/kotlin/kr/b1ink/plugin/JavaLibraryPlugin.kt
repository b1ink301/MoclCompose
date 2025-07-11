@file:Suppress("unused")

package kr.b1ink.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal class JavaLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(plugins) {
                apply("java-library")
                apply("org.jetbrains.kotlin.jvm")
            }
            apply<KotlinSerializationPlugin>()

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = Const.JAVA_VERSION
                targetCompatibility = Const.JAVA_VERSION
            }

            extensions.configure<KotlinProjectExtension> {
                jvmToolchain(Const.JDK_VERSION)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(Const.JVM_TARGET)
                }
            }

            dependencies {
//                "implementation"(libs.findLibrary("javax.inject").get())
                "implementation"(
                    libs.findLibrary("kotlinx.coroutines.core")
                        .get()
                )
                "implementation"(
                    libs.findLibrary("hilt.core")
                        .get()
                )
                "implementation"(
                    libs.findLibrary("paging.common")
                        .get()
                )
//                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(
                    libs.findLibrary("junit")
                        .get()
                )
            }
        }
    }
}