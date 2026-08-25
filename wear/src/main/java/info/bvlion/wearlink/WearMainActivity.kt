package info.bvlion.wearlink

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.tooling.preview.devices.WearDevices
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import info.bvlion.wearlink.wear.BuildConfig
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
  val showLocalNetworkAccessExplanationState = rememberSaveable { mutableStateOf(false) }

  WearLinkTheme {
    if (showLocalNetworkAccessExplanationState.value) {
      BackHandler {
        showLocalNetworkAccessExplanationState.value = false
      }
      LocalNetworkAccessExplanation(
        onGrant = {
          showLocalNetworkAccessExplanationState.value = false
          requestLocalNetworkPermission()
        },
        onBack = { showLocalNetworkAccessExplanationState.value = false }
      )
    } else {
      WearMainScreen(
        isLocalNetworkPermissionGranted = isLocalNetworkPermissionGranted,
        onRequestLocalNetworkPermission = { showLocalNetworkAccessExplanationState.value = true },
        startMobileActivity = startMobileActivity
      )
    }
  }
}

@Composable
private fun WearMainScreen(
  isLocalNetworkPermissionGranted: Boolean,
  onRequestLocalNetworkPermission: () -> Unit,
  startMobileActivity: () -> Unit,
) {
  val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
  ) {
    ScalingLazyColumn(
      state = listState,
      autoCentering = null,
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    ) {
      item {
        Text(
          text = stringResource(info.bvlion.wearlink.shared.R.string.app_name),
          style = MaterialTheme.typography.caption1,
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
        )
      }
      item {
        Chip(
          onClick = startMobileActivity,
          label = { Text(stringResource(R.string.main_launch_mobile)) },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        item {
          Text(
            text = stringResource(R.string.local_network_access),
            style = MaterialTheme.typography.caption1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 4.dp)
          )
        }
        item {
          if (isLocalNetworkPermissionGranted) {
            CompactChip(
              onClick = {},
              enabled = false,
              colors = ChipDefaults.secondaryChipColors(),
              label = { Text(stringResource(R.string.local_network_access_granted)) },
              modifier = Modifier.padding(horizontal = 16.dp)
            )
          } else {
            Chip(
              onClick = onRequestLocalNetworkPermission,
              label = { Text(stringResource(R.string.local_network_access_grant)) },
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
          }
        }
      }
      item {
        Text(
          text = stringResource(R.string.menu_title_version, BuildConfig.VERSION_NAME),
          style = MaterialTheme.typography.caption3,
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun LocalNetworkAccessExplanation(
  onGrant: () -> Unit,
  onBack: () -> Unit,
) {
  val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
  ) {
    ScalingLazyColumn(
      state = listState,
      autoCentering = null,
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 24.dp),
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    ) {
      item {
        Text(
          text = stringResource(R.string.local_network_access),
          style = MaterialTheme.typography.caption1,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp)
        )
      }
      item {
        Text(
          text = stringResource(R.string.local_network_access_description),
          style = MaterialTheme.typography.caption3,
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp)
        )
      }
      item {
        Chip(
          onClick = onGrant,
          label = { Text(stringResource(R.string.local_network_access_grant)) },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
      }
      item {
        Chip(
          onClick = onBack,
          colors = ChipDefaults.secondaryChipColors(),
          label = { Text(stringResource(R.string.local_network_access_back)) },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 6.dp, bottom = 4.dp)
        )
      }
    }
  }
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  name = "small round - ja - not granted"
)
@Composable
fun WearAppJaNotGrantedPreview() {
  WearApp(isLocalNetworkPermissionGranted = false)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  name = "small round - ja - granted"
)
@Composable
fun WearAppJaGrantedPreview() {
  WearApp(isLocalNetworkPermissionGranted = true)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  name = "small round - en - not granted"
)
@Composable
fun WearAppEnNotGrantedPreview() {
  WearApp(isLocalNetworkPermissionGranted = false)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  name = "small round - en - granted"
)
@Composable
fun WearAppEnGrantedPreview() {
  WearApp(isLocalNetworkPermissionGranted = true)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  fontScale = 1.24f,
  name = "small round - en - large font - not granted"
)
@Composable
fun WearAppEnNotGrantedLargeFontPreview() {
  WearApp(isLocalNetworkPermissionGranted = false)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  fontScale = 1.24f,
  name = "small round - en - large font - granted"
)
@Composable
fun WearAppEnGrantedLargeFontPreview() {
  WearApp(isLocalNetworkPermissionGranted = true)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  fontScale = 1.24f,
  name = "small round - ja - large font - not granted"
)
@Composable
fun WearAppJaNotGrantedLargeFontPreview() {
  WearApp(isLocalNetworkPermissionGranted = false)
}

@Preview(
  apiLevel = 37,
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  fontScale = 1.24f,
  name = "small round - ja - large font - granted"
)
@Composable
fun WearAppJaGrantedLargeFontPreview() {
  WearApp(isLocalNetworkPermissionGranted = true)
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  name = "small round - ja - explanation"
)
@Composable
fun LocalNetworkAccessExplanationJaPreview() {
  WearLinkTheme {
    LocalNetworkAccessExplanation(onGrant = {}, onBack = {})
  }
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  name = "small round - en - explanation"
)
@Composable
fun LocalNetworkAccessExplanationEnPreview() {
  WearLinkTheme {
    LocalNetworkAccessExplanation(onGrant = {}, onBack = {})
  }
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "en",
  fontScale = 1.24f,
  name = "small round - en - large font - explanation"
)
@Composable
fun LocalNetworkAccessExplanationEnLargeFontPreview() {
  WearLinkTheme {
    LocalNetworkAccessExplanation(onGrant = {}, onBack = {})
  }
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  locale = "ja",
  fontScale = 1.24f,
  name = "small round - ja - large font - explanation"
)
@Composable
fun LocalNetworkAccessExplanationJaLargeFontPreview() {
  WearLinkTheme {
    LocalNetworkAccessExplanation(onGrant = {}, onBack = {})
  }
}
