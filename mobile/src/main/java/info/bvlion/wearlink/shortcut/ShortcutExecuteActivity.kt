package info.bvlion.wearlink.shortcut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.request.RequestExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShortcutExecuteActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val requestId = intent.getStringExtra(RequestShortcuts.EXTRA_REQUEST_ID)
    if (requestId.isNullOrBlank()) {
      showResultAndFinish(getString(R.string.shortcut_request_not_found))
      return
    }

    lifecycleScope.launch {
      val request = withContext(Dispatchers.IO) {
        AppDataStore.getDataStore(application).getSavedRequest.first()
          ?.parseRequestParams()
          ?.findById(requestId)
      }
      if (request == null) {
        showResultAndFinish(getString(R.string.shortcut_request_not_found))
        return@launch
      }

      val response = RequestExecutor(application).execute(request) { getString(it) }
      val messageResId = if (response.responseCode in 200..299) {
        R.string.shortcut_request_success
      } else {
        R.string.shortcut_request_failure
      }
      showResultAndFinish(getString(messageResId, request.title))
    }
  }

  private fun showResultAndFinish(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    finish()
  }
}
