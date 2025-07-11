plugins {
    `kotlin-dsl`
}

group = "kr.b1ink.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
//    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.compose.compiler.plugins)
}

gradlePlugin {
    plugins {
        create("android-application") {
            id = "kr.b1ink.android.application"
            implementationClass = "kr.b1ink.plugin.AndroidApplicationPlugin"
        }
        create("android-compose") {
            id = "kr.b1ink.android.compose"
            implementationClass = "kr.b1ink.plugin.AndroidComposePlugin"
        }
        create("kotlin") {
            id = "kr.b1ink.kotlin"
            implementationClass = "kr.b1ink.plugin.AndroidKotlinPlugin"
        }
        create("java-library") {
            id = "kr.b1ink.java.library"
            implementationClass = "kr.b1ink.plugin.JavaLibraryPlugin"
        }
        create("android-hilt") {
            id = "kr.b1ink.android.hilt"
            implementationClass = "kr.b1ink.plugin.AndroidHiltPlugin"
        }
        create("android-library") {
            id = "kr.b1ink.android.library"
            implementationClass = "kr.b1ink.plugin.AndroidLibraryPlugin"
        }
        create("android-feature") {
            id = "kr.b1ink.feature"
            implementationClass = "kr.b1ink.plugin.AndroidFeaturePlugin"
        }
    }
}