package info.bvlion.wearlink

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.tooling.preview.devices.WearDevices
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.ui.theme.WearLinkTheme

class WearMainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent {
      WearApp {
        AppConstants.startMobileActivity(
          this,
          successProcess = {
            Toast.makeText(this, getString(R.string.main_launched_mobile), Toast.LENGTH_SHORT).show()
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
  WearLinkTheme {
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
          stringResource(R.string.main_launch_mobile),
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
  WearApp()
}