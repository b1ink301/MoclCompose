package kr.b1ink.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9.0+ handles Kotlin natively - no need for kotlin-android plugin
        // compileSdk, minSdk, compileOptions, packaging are configured in KotlinAndroid.kt

        extensions.getByType(KotlinAndroidProjectExtension::class.java).apply {
            compilerOptions {
                jvmTarget.set(Const.JVM_TARGET)
            }
        }

        dependencies {
            "implementation"(libs.findLibrary("kotlin.stdlib").get())
            "implementation"(libs.findLibrary("kotlinx.coroutines").get())
            "implementation"(libs.findLibrary("kotlinx.datetime").get())
        }
    }
}
