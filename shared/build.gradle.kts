import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
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
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget("17")
  }
}

dependencies {
  implementation(libs.play.services.wearable)
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.kotlin.parcelize.runtime)
  implementation(libs.androidx.ui)
  implementation(libs.datastore.preferences)
  implementation(platform(libs.androidx.compose.bom))

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics.ktx)

  add("kotlinCompilerPluginClasspath", libs.kotlin.parcelize.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.json)
}
