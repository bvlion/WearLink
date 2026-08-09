package info.bvlion.wearlink.httpexecute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.request.HttpRequester
import info.bvlion.wearlink.request.executeRequest
import info.bvlion.wearlink.request.hasLocalNetworkAccessPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MINIMUM_LOADING_MILLIS = 2000L

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
      val responseDeferred = async {
        executeRequest(
          requester,
          request,
          isMobile = false,
          hasLocalNetworkPermission = getApplication<Application>().hasLocalNetworkAccessPermission(),
          getString = getString
        )
      }
      delay(MINIMUM_LOADING_MILLIS)
      val response = responseDeferred.await()

      dataStore.appendResponse(response)
      _isSent.value = true
    }
  }
}
