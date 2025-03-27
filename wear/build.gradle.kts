plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics.gradle)
}

android {
  namespace = "info.bvlion.wearlink"
  compileSdk = providers.gradleProperty("COMPILE_SDK").get().toInt()

  defaultConfig {
    applicationId = "info.bvlion.wearlink"
    minSdk = providers.gradleProperty("WEAR_MIN_SDK").get().toInt()
    targetSdk = providers.gradleProperty("TARGET_SDK").get().toInt()
    versionCode = 200000000 + providers.gradleProperty("VERSION_CODE").get().toInt()
    versionName = providers.gradleProperty("VERSION_NAME").get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      isDebuggable = true
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(project(":shared"))
  implementation(project(":AppInfoManager"))
  implementation(libs.play.services.wearable)
  implementation(libs.wear)
  implementation(libs.wear.remote.interactions)
  implementation(libs.androidx.ui)
  implementation(libs.wear.compose.material.core)
  implementation(libs.wear.compose.foundation)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.wear.tiles)
  implementation(libs.androidx.wear.protolayout.material)
  implementation(libs.horologist.compose.tools)
  implementation(libs.horologist.tiles)
  implementation(libs.core.splashscreen)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics.ktx)
  implementation(libs.firebase.analytics.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.datastore.preferences)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.wear.tooling.preview)
  implementation(libs.androidx.wear.tiles.tooling.preview)

  testImplementation(libs.junit)

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)

  debugImplementation(libs.androidx.ui.tooling)
}