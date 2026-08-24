package info.bvlion.wearlink.httpexecute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
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
    val scrollState = rememberScrollState()
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      positionIndicator = { PositionIndicator(scrollState = scrollState) },
    ) {
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
          modifier = Modifier
            .verticalScroll(scrollState)
            .padding(40.dp),
          style = MaterialTheme.typography.body2,
          textAlign = TextAlign.Center,
          text = stringResource(R.string.execute_message, title)
        )
      }
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
    val listState = rememberScalingLazyListState()
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
      ScalingLazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colors.background),
      ) {
        item {
          Text(
            text = stringResource(R.string.execute_message_confirm, title),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
          )
        }
        item {
          Chip(
            onClick = onFinish,
            colors = ChipDefaults.secondaryChipColors(),
            label = { Text(stringResource(R.string.execute_message_confirm_no)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp)
          )
        }
        item {
          Chip(
            onClick = onExecute,
            label = { Text(stringResource(R.string.execute_message_confirm_yes)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 6.dp, bottom = 4.dp)
          )
        }
      }
    }
  }
}

private const val PREVIEW_LONG_REQUEST_TITLE =
  "とても長いRequest名を表示するテストケース用サンプルタイトルです"

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  name = "small round - normal font - normal title"
)
@Composable
fun HttpExecuteConfirmPreview() {
  HttpExecuteConfirm("玄関の鍵")
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  name = "small round - normal font - long title"
)
@Composable
fun HttpExecuteConfirmLongTitlePreview() {
  HttpExecuteConfirm(PREVIEW_LONG_REQUEST_TITLE)
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  fontScale = 2.0f,
  name = "small round - large font - normal title"
)
@Composable
fun HttpExecuteConfirmLargeFontPreview() {
  HttpExecuteConfirm("玄関の鍵")
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  fontScale = 2.0f,
  name = "small round - large font - long title"
)
@Composable
fun HttpExecuteConfirmLargeFontLongTitlePreview() {
  HttpExecuteConfirm(PREVIEW_LONG_REQUEST_TITLE)
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  name = "small round - normal font"
)
@Composable
fun HttpExecutePreview() {
  HttpExecute("玄関の鍵")
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  fontScale = 2.0f,
  name = "small round - large font - normal title"
)
@Composable
fun HttpExecuteLargeFontPreview() {
  HttpExecute("玄関の鍵")
}

@Preview(
  device = WearDevices.SMALL_ROUND,
  showSystemUi = true,
  fontScale = 2.0f,
  name = "small round - large font - long title"
)
@Composable
fun HttpExecuteLargeFontLongTitlePreview() {
  HttpExecute(PREVIEW_LONG_REQUEST_TITLE)
}
