package info.bvlion.wearlink.httpexecute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.request.HttpRequester
import info.bvlion.wearlink.request.executeCatching
import info.bvlion.wearlink.request.hasLocalNetworkAccessPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HttpExecuteViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()

  private val _isSent = MutableStateFlow(false)
  val isSent = _isSent.asStateFlow()

  fun sendRequest(param: String?, getString: (Int) -> String) {
    if (param.isNullOrEmpty()) {
      _isSent.value = true
      return
    }

    val request = param.parseRequestParam()
    viewModelScope.launch(Dispatchers.IO) {
      val networkDeferred = async(Dispatchers.IO) {
        requester.executeCatching(
          request,
          isMobile = false,
          hasLocalNetworkPermission = getApplication<Application>().hasLocalNetworkAccessPermission(),
          getString = getString
        )
      }
      val timerDeferred = async(Dispatchers.IO) {
        delay(2000)
      }

      val response = listOf(networkDeferred, timerDeferred).awaitAll()
        .filterIsInstance<ResponseParams>()
        .first()

      dataStore.appendResponse(response)
      _isSent.value = true
    }
  }
}
