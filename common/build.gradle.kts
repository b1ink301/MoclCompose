plugins {
    customPlugin("android.library")
    customPlugin("android.compose")
}

android {
    namespace = "kr.b1ink.common"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    //test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.constraintlayout)

    //permissions
    implementation(libs.tedpermission.normal)

    //in-app update
    implementation(libs.app.update.ktx)

    //json
    implementation(libs.gson)
}