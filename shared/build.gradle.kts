plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
}

android {
  namespace = "info.bvlion.wearlink.shared"
  compileSdk = providers.gradleProperty("COMPILE_SDK").get().toInt()

  buildTypes {
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
}

dependencies {
  implementation(libs.play.services.wearable)
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.androidx.ui)
  implementation(libs.datastore.preferences)
  implementation(platform(libs.androidx.compose.bom))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics.ktx)
  implementation(libs.firebase.analytics.ktx)

  testImplementation(libs.junit)
  testImplementation(libs.json)
}