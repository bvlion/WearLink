package net.ambitious.android.httprequesttile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import net.ambitious.android.httprequesttile.data.AppConstants
import net.ambitious.android.httprequesttile.theme.HttpRequestTileTheme

class WearMainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent {
      WearApp {
        AppConstants.startMobileActivity(
          this,
          lifecycleScope,
          successProcess = {
            Toast.makeText(this, "スマートフォンのアプリを起動しました", Toast.LENGTH_SHORT).show()
          }
        ) {
          ConfirmationOverlay()
            .setType(ConfirmationOverlay.FAILURE_ANIMATION)
            .showOn(this)
        }
      }
    }
  }
}

@Composable
fun WearApp(startMobileActivity: () -> Unit = {}) {
  HttpRequestTileTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
      verticalArrangement = Arrangement.Center
    ) {
      Button(
        onClick = startMobileActivity,
        modifier = Modifier.fillMaxSize().padding(16.dp)
      ) {
        Text(
          "スマートフォンを\n起動する",
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
  WearApp()
}