package info.bvlion.wearlink.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import info.bvlion.wearlink.MobileMainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().setKeepOnScreenCondition { false }
    super.onCreate(savedInstanceState)
    startActivity(Intent(this, MobileMainActivity::class.java))
    finish()
  }
}