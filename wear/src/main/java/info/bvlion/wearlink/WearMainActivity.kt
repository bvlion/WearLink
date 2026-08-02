package info.bvlion.wearlink

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.tooling.preview.devices.WearDevices
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import info.bvlion.wearlink.wear.R

class WearMainActivity : ComponentActivity() {
  private val isLocalNetworkPermissionGranted = mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent {
      val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) {
        isLocalNetworkPermissionGranted.value = ContextCompat.checkSelfPermission(
          this@WearMainActivity,
          Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
      }
      WearApp(
        isLocalNetworkPermissionGranted = isLocalNetworkPermissionGranted.value,
        requestLocalNetworkPermission = {
          if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            !isLocalNetworkPermissionGranted.value
          ) {
            localNetworkPermissionLauncher.launch(
              Manifest.permission.ACCESS_LOCAL_NETWORK
            )
          }
        },
        startMobileActivity = {
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
      )
    }
  }

  override fun onResume() {
    super.onResume()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
      isLocalNetworkPermissionGranted.value = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_LOCAL_NETWORK
      ) == PackageManager.PERMISSION_GRANTED
    }
  }
}

@Composable
fun WearApp(
  isLocalNetworkPermissionGranted: Boolean = false,
  requestLocalNetworkPermission: () -> Unit = {},
  startMobileActivity: () -> Unit = {},
) {
  WearLinkTheme {
    ScalingLazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    ) {
      item {
        Chip(
          onClick = startMobileActivity,
          label = { Text(stringResource(R.string.main_launch_mobile)) },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        item {
          Chip(
            onClick = requestLocalNetworkPermission,
            label = { Text(stringResource(R.string.local_network_access)) },
            secondaryLabel = {
              Text(
                stringResource(
                  if (isLocalNetworkPermissionGranted) {
                    R.string.local_network_access_granted
                  } else {
                    R.string.local_network_access_grant
                  }
                )
              )
            },
            enabled = !isLocalNetworkPermissionGranted,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
          )
        }
      }
    }
  }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
  WearApp()
}
