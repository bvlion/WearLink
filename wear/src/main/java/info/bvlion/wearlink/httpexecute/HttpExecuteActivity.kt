package info.bvlion.wearlink.httpexecute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import info.bvlion.wearlink.wear.R
import info.bvlion.wearlink.ui.theme.WearLinkTheme

class HttpExecuteActivity : ComponentActivity() {

  private val viewModel by viewModels<HttpExecuteViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val title = intent.getStringExtra(EXTRA_REQUEST_TITLE) ?: ""
      val showConfirmation = intent.getBooleanExtra(EXTRA_SHOW_CONFIRMATION, false)

      val showConfirmationState = remember { mutableStateOf(showConfirmation) }

      if (showConfirmationState.value) {
        HttpExecuteConfirm(
          title = title,
          onExecute = { showConfirmationState.value = false },
          onFinish = { finish() }
        )
      } else {
        HttpExecute(title)
        LaunchedEffect(Unit) {
          viewModel.sendRequest(intent.getStringExtra(EXTRA_REQUEST_PARAMS)) {
            getString(it)
          }
        }

        val isSent = viewModel.isSent.collectAsState()
        if (isSent.value) {
          finish()
        }
      }
    }
  }

  companion object {
    internal const val EXTRA_SHOW_CONFIRMATION = "showConfirmation"
    internal const val EXTRA_REQUEST_TITLE = "requestTitle"
    internal const val EXTRA_REQUEST_PARAMS = "requestParams"
  }
}

@Composable
fun HttpExecute(title: String) {
  WearLinkTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        indicatorColor = MaterialTheme.colors.secondary,
        trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
        strokeWidth = 12.dp,
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colors.background)
      )
      Text(
        modifier = Modifier.padding(40.dp),
        fontSize = 14.sp,
        text = stringResource(R.string.execute_message, title)
      )
    }
  }
}

@Composable
fun HttpExecuteConfirm(
  title: String,
  onExecute: () -> Unit = {},
  onFinish: () -> Unit = {}
) {
  WearLinkTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .background(MaterialTheme.colors.background),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        fontSize = 14.sp,
        text = stringResource(R.string.execute_message_confirm, title)
      )
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        Button(
          onClick = onFinish,
          modifier = Modifier.padding(end = 8.dp)
        ) {
          Text(
            text = stringResource(R.string.execute_message_confirm_no),
            fontSize = 12.sp,
          )
        }
        Button(onClick = onExecute) {
          Text(
            text = stringResource(R.string.execute_message_confirm_yes),
            fontSize = 12.sp,
          )
        }
      }
    }
  }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HttpExecuteConfirmPreview() {
  HttpExecuteConfirm("玄関の鍵")
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HttpExecutePreview() {
  HttpExecute("玄関の鍵")
}
