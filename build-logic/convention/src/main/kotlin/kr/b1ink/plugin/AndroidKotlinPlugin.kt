package kr.b1ink.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(plugins) {
            apply("kotlin-android")
        }

        extensions.getByType<BaseExtension>().apply {
            setCompileSdkVersion(libs.findVersion("compileSdk").get().requiredVersion.toInt())

            defaultConfig {
                minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = false
                sourceCompatibility = Const.JAVA_VERSION
                targetCompatibility = Const.JAVA_VERSION
            }
            packagingOptions {
                resources {
                    excludes.apply {
                        add("/META-INF/AL2.0")
                        add("/META-INF/LGPL2.1")
                        add("kotlin/reflect/*")
                    }
                    pickFirsts.add("lib/*/*.so")
                }
            }
        }
        extensions.getByType<KotlinAndroidProjectExtension>().apply {
            compilerOptions {
                jvmTarget.set(Const.JVM_TARGET)
            }
        }

        dependencies {
//            "coreLibraryDesugaring"(libs.findLibrary("desugar.jdk.libs").get())
            "implementation"(libs.findLibrary("kotlin.stdlib").get())
            "implementation"(libs.findLibrary("kotlinx.coroutines").get())
            "implementation"(libs.findLibrary("kotlinx.datetime").get())
        }
    }
}