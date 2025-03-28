plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics.gradle)
}

android {
  namespace = "info.bvlion.wearlink.mobile"
  compileSdk = providers.gradleProperty("COMPILE_SDK").get().toInt()

  defaultConfig {
    applicationId = "info.bvlion.wearlink"
    minSdk = providers.gradleProperty("MIN_SDK").get().toInt()
    targetSdk = providers.gradleProperty("TARGET_SDK").get().toInt()
    versionCode = 100000000 + providers.gradleProperty("VERSION_CODE").get().toInt()
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
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

dependencies {
  implementation(project(":shared"))
  implementation(project(":AppInfoManager"))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.play.services.wearable)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics.ktx)
  implementation(libs.firebase.analytics.ktx)
  implementation(libs.firebase.firestore.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.datastore.preferences)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.android.material)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.splashscreen)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
}