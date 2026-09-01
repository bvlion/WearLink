import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "info.bvlion.wearlink.wear"
  compileSdk = providers.gradleProperty("WEAR_COMPILE_SDK").get().toInt()

  defaultConfig {
    applicationId = "net.ambitious.android.wearlink"
    minSdk = providers.gradleProperty("WEAR_MIN_SDK").get().toInt()
    targetSdk = providers.gradleProperty("WEAR_TARGET_SDK").get().toInt()
    versionCode = 200000000 + providers.gradleProperty("VERSION_CODE").get().toInt()
    versionName = providers.gradleProperty("VERSION_NAME").get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      storeFile = file("../release.jks")
      storePassword = System.getenv()["KEYSTORE_PASSWORD"]
      keyAlias = System.getenv()["KEYSTORE_ALIAS"]
      keyPassword = System.getenv()["KEYSTORE_PASSWORD"]
    }
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
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget("17")
  }
}

dependencies {
  implementation(project(":shared"))
  implementation(libs.play.services.wearable)
  implementation(libs.kotlinx.coroutines.play.services)
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

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.datastore.preferences)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.wear.tooling.preview)
  implementation(libs.androidx.wear.tiles.tooling.preview)
  implementation(libs.androidx.watchface.complications.data.source.ktx)

  testImplementation(libs.junit)

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)

  debugImplementation(libs.androidx.ui.tooling)
}
