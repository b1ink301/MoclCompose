import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties

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
            val userKeystoreFile = project.rootProject.file("../keystore/key.properties")
            val keystoreProperties = Properties()
            val hasKeyInfo = userKeystoreFile.exists()

            if (hasKeyInfo) {
                userKeystoreFile.reader()
                    .use { reader ->
                        keystoreProperties.load(reader)
                    }
                // 필요한 속성이 모두 존재하는지 검증
                storeFile = file(
                    keystoreProperties.getProperty("storeFile")
                        ?: throw IllegalArgumentException("Missing 'storeFile' in key.properties")
                )
                keyAlias = keystoreProperties.getProperty("keyAlias")
                    ?: throw IllegalArgumentException("Missing 'keyAlias' in key.properties")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                    ?: throw IllegalArgumentException("Missing 'keyPassword' in key.properties")
                storePassword = keystoreProperties.getProperty("storePassword")
                    ?: throw IllegalArgumentException("Missing 'storePassword' in key.properties")
            } else {
                project.logger.info("Keystore file not found. Trying to load from environment variables.")

                val storeFileEnv = System.getenv("KEYSTORE_FILE_PATH")
                val keyAliasEnv = System.getenv("KEY_ALIAS")
                val keyPasswordEnv = System.getenv("KEY_PASSWORD")
                val storePasswordEnv = System.getenv("KEYSTORE_PASSWORD")

                if (storeFileEnv != null && keyAliasEnv != null && keyPasswordEnv != null && storePasswordEnv != null) {
                    storeFile = file(storeFileEnv)
                    keyAlias = keyAliasEnv
                    keyPassword = keyPasswordEnv
                    storePassword = storePasswordEnv

                    project.logger.info("Successfully loaded keystore properties from environment variables.")
                } else {
                    val missingEnvVars = mutableListOf<String>()
                    if (storeFileEnv == null) missingEnvVars.add("KEYSTORE_FILE_PATH")
                    if (keyAliasEnv == null) missingEnvVars.add("KEY_ALIAS")
                    if (keyPasswordEnv == null) missingEnvVars.add("KEY_PASSWORD")
                    if (storePasswordEnv == null) missingEnvVars.add("KEYSTORE_PASSWORD")

                    throw GradleException(
                        "Keystore file not found at ${userKeystoreFile.absolutePath} and " +
                                "one or more required environment variables are missing: ${missingEnvVars.joinToString()}. " +
                                "Please provide the keystore file or set the following environment variables: " +
                                "KEYSTORE_FILE_PATH, KEY_ALIAS, KEY_PASSWORD, KEYSTORE_PASSWORD"
                    )
                }
            }
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
