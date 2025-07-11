@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id("com.android.test")
  id("androidx.baselineprofile")
}

android {
  namespace = "kr.b1ink.benchmark"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
    //    freeCompilerArgs += ["-Xopt-in=kotlin.RequiresOptIn"]
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

//  buildTypes {
//    // This benchmark buildType is used for benchmarking, and should function like your
//    // release build (for example, with minification on). It's signed with a debug key
//    // for easy local/CI testing.
//    benchmark {
//      debuggable = true
//      signingConfig = getByName("debug").signingConfig
//      matchingFallbacks = ["release"]
//    }
//  }
//
  targetProjectPath = ":app"
//  experimentalProperties["android.experimental.self-instrumenting"] = true

  testOptions.managedDevices.devices {
    pixel6Api31(com.android.build.api.dsl.ManagedVirtualDevice) {
      device = "Pixel 6"
      apiLevel = 31
      systemImageSource = "aosp"
    }
  }
}

dependencies {
//  implementation 'androidx.test.ext:junit:1.1.5'
//  implementation junitExt
  implementation(libs.espresso.core)
  implementation(libs.uiautomator)
//  implementation 'androidx.benchmark:benchmark-macro-junit4:1.1.1'
  implementation(libs.profileinstaller)
  implementation(libs.macrobenchmark)
//  implementation composeProfileInstaller
//  implementation testRules
}

baselineProfile {
  // Specifies the GMDs to run the tests on. The default is none.
  managedDevices += "pixel6Api31"
  // Enables using connected devices to generate profiles. The default is
  // `true`. When using connected devices, they must be rooted or API 33 and
  // higher.
  useConnectedDevices = false
}


//androidComponents {
//  beforeVariants(selector().all()) {
//    enabled = buildType == "benchmark"
//  }
//}