plugins {
    customPlugin("android.library")
    customPlugin("android.compose")
}

android {
    namespace = "com.ireward.htmlcompose"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmarkRelease") {
            initWith(getByName("release"))
            isMinifyEnabled = false
            matchingFallbacks += "release"
        }
        create("nonMinifiedRelease") {
            initWith(getByName("release"))
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    api(libs.bundles.coil3)

    //video
    implementation(libs.sanghun.compose.video)
    implementation(libs.media3.session)
    implementation(libs.media3.player)
    implementation(libs.media3.ui)

    implementation(libs.fleeksoft.ksoup)
    implementation(libs.androidyoutubeplayer.core)

//    implementation(libs.scale.image.viewer)
}