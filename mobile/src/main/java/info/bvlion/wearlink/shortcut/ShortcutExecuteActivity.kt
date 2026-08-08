package info.bvlion.wearlink.shortcut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.ui.theme.WearLinkTheme

class ShortcutExecuteActivity : ComponentActivity() {
  private val viewModel by viewModels<ShortcutExecuteViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val requestId = intent.getStringExtra(RequestShortcuts.EXTRA_REQUEST_ID)
    viewModel.execute(requestId) { getString(it) }

    setContent {
      val state by viewModel.state.collectAsState()
      val viewMode by viewModel.viewMode.collectAsState()
      val isSystemInDarkTheme = isSystemInDarkTheme()
      val isDarkMode = AppConstants.isDarkMode(viewMode, isSystemInDarkTheme)

      WearLinkTheme(isDarkMode) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
          contentAlignment = Alignment.Center
        ) {
          val loadingState = state as? ShortcutExecuteState.Loading
          if (loadingState != null) {
            Card(
              modifier = Modifier.padding(horizontal = 48.dp),
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
                loadingState.title?.let {
                  Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp)
                  )
                }
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

      LaunchedEffect(state) {
        val message = when (val current = state) {
          is ShortcutExecuteState.Success -> getString(R.string.shortcut_request_success, current.title)
          is ShortcutExecuteState.Failure -> getString(R.string.shortcut_request_failure, current.title)
          ShortcutExecuteState.RequestNotFound -> getString(R.string.shortcut_request_not_found)
          is ShortcutExecuteState.Loading -> null
        }
        if (message != null) {
          finish()
          Toast.makeText(this@ShortcutExecuteActivity, message, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}
