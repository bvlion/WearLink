package net.ambitious.android.httprequesttile.httpexecute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import net.ambitious.android.httprequesttile.theme.HttpRequestTileTheme

class HttpExecuteActivity : ComponentActivity() {

  private val viewModel by viewModels<HttpExecuteViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val isSent = viewModel.isSent.collectAsState()
      HttpExecute(intent.getStringExtra(EXTRA_REQUEST_TITLE) ?: "")
      viewModel.sendRequest(intent.getStringExtra(EXTRA_REQUEST_PARAMS))
      if (isSent.value) {
        intent = null // 不用意に呼ばれるのを防止する
        finish()
      }
    }
  }

  companion object {
    internal const val EXTRA_REQUEST_TITLE = "requestTitle"
    internal const val EXTRA_REQUEST_PARAMS = "requestParams"
  }
}

@Composable
fun HttpExecute(title: String) {
  HttpRequestTileTheme {
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
        text = "「$title」を実行しています…"
      )
    }
  }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun HttpExecutePreview() {
  HttpExecute("玄関の鍵")
}
