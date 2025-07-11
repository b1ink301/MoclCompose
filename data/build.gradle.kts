plugins {
    customPlugin("android.library")
}

android {
    namespace = "kr.b1ink.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        testInstrumentationRunner = "com.example.hilttest.HiltTestRunner"
//        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "CLIEN_API_URL", "\"https://m.clien.net/service/\"")
        buildConfigField("String", "NAVER_CAFE_API_URL", "\"https://apis.naver.com/\"")
        buildConfigField("String", "DAMOANG_API_URL", "\"https://damoang.net/\"")
        buildConfigField("String", "MEECO_API_URL", "\"https://meeco.kr/\"")
    }

    buildTypes {
        getByName("release") {
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
        getByName("debug") {
        }
//        create("beta") {
//            initWith(getByName("release"))
//        }
    }
}

dependencies {
//  api(fileTree("libs") { include("*.jar") })

    //preference
    implementation(libs.bundles.kotpref)

    api(libs.kotlinx.immutable)

    //http
    implementation(libs.bundles.retrofit)

    //room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.espresso)
    ksp(libs.room.compiler)

    //worker
    api(libs.hilt.work)
    api(libs.work.runtime.ktx)

    //paging
    api(libs.paging.runtime)

    //test
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
//    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.robolectric)
    testImplementation(libs.hamcrest)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.junit)
    kspTest(libs.hilt.compiler)
    testImplementation(libs.mockk)
//    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.okhttp3.mockwebserver)
    androidTestImplementation(libs.androidx.runner)
    testImplementation(libs.androidx.rules)
    testImplementation(libs.androidx.arch.core)
}