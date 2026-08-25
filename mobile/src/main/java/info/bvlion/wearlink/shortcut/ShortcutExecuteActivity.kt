package info.bvlion.wearlink.shortcut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.ui.theme.WearLinkTheme

class ShortcutExecuteActivity : ComponentActivity() {
  private val viewModel by viewModels<ShortcutExecuteViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val requestId = intent.getStringExtra(RequestShortcuts.EXTRA_REQUEST_ID)
    viewModel.execute(requestId) { getString(it) }

    setContent {
      val state by viewModel.state.collectAsState()
      val viewMode by viewModel.viewMode.collectAsState()
      val isSystemInDarkTheme = isSystemInDarkTheme()
      val isDarkMode = AppConstants.isDarkMode(viewMode, isSystemInDarkTheme)

      val contentAlpha = remember { Animatable(1f) }

      WearLinkTheme(isDarkMode) {
        ShortcutExecuteScreen(title = state.title, contentAlpha = contentAlpha.value)
      }

      LaunchedEffect(state) {
        val message = when (val current = state) {
          is ShortcutExecuteState.Success -> getString(R.string.shortcut_request_success, current.title)
          is ShortcutExecuteState.Failure -> getString(R.string.shortcut_request_failure, current.title)
          ShortcutExecuteState.RequestNotFound -> getString(R.string.shortcut_request_not_found)
          is ShortcutExecuteState.Loading -> null
        }

        if (message != null) {
          contentAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 250)
          )
          finish()
          Toast.makeText(this@ShortcutExecuteActivity, message, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}

@Composable
private fun ShortcutExecuteScreen(title: String?, contentAlpha: Float) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .alpha(contentAlpha)
      .background(Color.Black.copy(alpha = 0.35f)),
    contentAlignment = Alignment.Center
  ) {
    // 非常に長いRequest名や改行、大きいfont scaleでもCardが画面の外へはみ出さないよう上限を設ける
    val cardMaxHeight = maxHeight * 0.8f

    title?.let {
      Card(
        modifier = Modifier
          .padding(horizontal = 48.dp)
          .heightIn(max = cardMaxHeight),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = it,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .weight(weight = 1f, fill = false)
              .verticalScroll(rememberScrollState())
              .padding(top = 12.dp)
          )
          Text(
            text = stringResource(R.string.shortcut_execute_sending),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ShortcutExecuteScreenPreview() {
  WearLinkTheme {
    ShortcutExecuteScreen(
      title = "定期実行リクエスト",
      contentAlpha = 1f
    )
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ShortcutExecuteScreenLongTitlePreview() {
  WearLinkTheme {
    ShortcutExecuteScreen(
      title = "非常に長いRequest名のストレステスト用文字列玄関の鍵長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い長い",
      contentAlpha = 1f
    )
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ShortcutExecuteScreenMultilineTitlePreview() {
  WearLinkTheme {
    ShortcutExecuteScreen(
      title = (1..30).joinToString(separator = "\n") { "改行ストレステスト行$it" },
      contentAlpha = 1f
    )
  }
}
