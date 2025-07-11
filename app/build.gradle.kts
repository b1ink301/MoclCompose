import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.io.FileNotFoundException
import java.util.Properties
import kotlin.apply

plugins {
    customPlugin("android.application")
    customPlugin("android.compose")
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "kr.b1ink.mocl"

    defaultConfig {
        applicationId = "kr.b1ink.mocl"
        versionCode = libs.versions.appVersionCode.get()
            .toInt()
        versionName = "1.3.$versionCode"
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }

    signingConfigs {
        create("mocl") {
            val keyPropertiesFile = project.rootProject.file("../keystore/key.properties")
            if (!keyPropertiesFile.exists()) {
                throw FileNotFoundException("key.properties file not found at ${keyPropertiesFile.absolutePath}")
            }
            val properties = Properties().apply {
                load(keyPropertiesFile.reader())
            }
            // 필요한 속성이 모두 존재하는지 검증
            storeFile = file(properties.getProperty("storeFile") ?: throw IllegalArgumentException("Missing 'storeFile' in key.properties"))
            keyAlias = properties.getProperty("keyAlias") ?: throw IllegalArgumentException("Missing 'keyAlias' in key.properties")
            keyPassword = properties.getProperty("keyPassword") ?: throw IllegalArgumentException("Missing 'keyPassword' in key.properties")
            storePassword = properties.getProperty("storePassword") ?: throw IllegalArgumentException("Missing 'storePassword' in key.properties")
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("mocl")
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Mocl.dev")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("mocl")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "benchmark-proguard-rules.pro"
            )
            isShrinkResources = true

            //noinspection WrongGradleMethod
            firebaseAppDistribution {
                appId = "1:275270612301:android:6b15e1c81167409a"
                artifactType = "AAB"
                releaseNotesFile = "./release_notes.txt"
                testers = "b1ink301@gmail.com, b1ink301.work@gmail.com"
//                serviceCredentialsFile = "app/google-services.json"
            }
        }
    }

    applicationVariants.all {
        val buildTypeName = buildType.name
        val outputFileName = "mocl_${buildTypeName}_${versionName}.apk"

        outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = outputFileName
            }
    }
}

baselineProfile {
    automaticGenerationDuringBuild = true
    saveInSrc = true
}

dependencies {
    implementation(files("libs/fragula-common-release.aar"))
    implementation(files("libs/fragula-compose-release.aar"))

    implementation(project(":common"))
    implementation(project(":htmlcompose"))
    baselineProfile(project(":baselineprofile"))

    //worker
    implementation(libs.androidx.startup)

    //paging
    implementation(libs.paging.compose)

    //profile
    implementation(libs.macrobenchmark)
    implementation(libs.profileinstaller)

    //swipe-back
//    implementation(libs.fragula.compose)

    //test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    //splash
    implementation(libs.androidx.core.splashscreen)

    //webview
    implementation(libs.androidx.browser)
    implementation(libs.kevinnzou.compose.webview)

    implementation(libs.firebase.appdistribution.api.ktx)
//  "betaImplementation"(libs.firebase.appdistribution)

//    implementation(libs.androidx.tracing.perfetto)
//    implementation(libs.androidx.tracing.perfetto.binary)

    implementation(libs.bundles.mvi)
    testImplementation(libs.orbit.test)

    implementation(libs.androidx.material.icons.extended.android)
}
