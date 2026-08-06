package info.bvlion.wearlink.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import info.bvlion.wearlink.MobileMainActivity
import info.bvlion.wearlink.shortcut.RequestShortcuts
import info.bvlion.wearlink.shortcut.ShortcutExecuteActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().setKeepOnScreenCondition { false }
    super.onCreate(savedInstanceState)
    val destination = if (intent?.action == RequestShortcuts.ACTION_EXECUTE_SHORTCUT) {
      Intent(this, ShortcutExecuteActivity::class.java).apply {
        putExtra(RequestShortcuts.EXTRA_REQUEST_ID, intent.getStringExtra(RequestShortcuts.EXTRA_REQUEST_ID))
      }
    } else {
      Intent(this, MobileMainActivity::class.java)
    }
    startActivity(destination)
    finish()
  }
}
