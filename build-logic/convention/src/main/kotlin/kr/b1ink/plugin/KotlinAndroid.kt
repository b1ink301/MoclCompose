package kr.b1ink.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * 공통 Kotlin/Android 플러그인 적용 및 의존성 설정.
 * Android extension 설정은 각 호출자에서 직접 수행 (AGP 9.0+ 호환).
 */
internal fun Project.configureKotlinAndroid() {
    apply<AndroidKotlinPlugin>()
    apply<KotlinSerializationPlugin>()
    with(plugins) {
        apply("kotlin-parcelize")
    }
    apply<AndroidHiltPlugin>()

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

/** Application 모듈용 공통 Android 설정 */
internal fun ApplicationExtension.configureAndroidCommon(project: Project) {
    compileSdk = project.libs.findVersion("compileSdk").get().requiredVersion.toInt()

    defaultConfig {
        minSdk = project.libs.findVersion("minSdk").get().requiredVersion.toInt()
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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-debug.pro",
            )
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures.apply {
        viewBinding = true
        buildConfig = true
    }
}

/** Library 모듈용 공통 Android 설정 */
internal fun LibraryExtension.configureAndroidCommon(project: Project) {
    compileSdk = project.libs.findVersion("compileSdk").get().requiredVersion.toInt()

    defaultConfig {
        minSdk = project.libs.findVersion("minSdk").get().requiredVersion.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.fullTracing.enable"] = "true"
    }

    compileOptions {
        sourceCompatibility = Const.JAVA_VERSION
        targetCompatibility = Const.JAVA_VERSION
    }

    buildTypes {
        getByName("debug") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-debug.pro",
            )
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures.apply {
        viewBinding = true
        buildConfig = true
    }
}
