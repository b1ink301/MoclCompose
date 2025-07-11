package kr.b1ink.plugin

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureKotlinAndroid() {
    apply<AndroidKotlinPlugin>()
    apply<KotlinSerializationPlugin>()
    with(plugins) {
        apply("kotlin-parcelize")
    }
    apply<AndroidHiltPlugin>()

    extensions.configure(CommonExtension::class.java) {
        compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables {
                useSupportLibrary = true
            }

            testInstrumentationRunnerArguments["androidx.benchmark.fullTracing.enable"] = "true"
        }

        compileOptions {
            sourceCompatibility = Const.JAVA_VERSION
            targetCompatibility = Const.JAVA_VERSION
        }

        buildTypes {
            getByName("debug") {
                proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-debug.pro",
                )
            }

            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro",
                )
            }
        }

        buildFeatures.apply {
            viewBinding = true
            buildConfig = true
        }
    }

    dependencies {
        "implementation"(project(":domain"))
        "implementation"(libs.findLibrary("core.ktx").get())
        "implementation"(libs.findLibrary("appcompat").get())
        "implementation"(libs.findLibrary("timber").get())
        "implementation"(libs.findLibrary("constraintlayout").get())
        "implementation"(libs.findBundle("kotlinx.coroutines").get())
        "implementation"(libs.findBundle("androidx.lifecycle").get())
    }
}

