package info.bvlion.wearlink.shortcut

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.request.HttpRequester
import info.bvlion.wearlink.request.executeRequest
import info.bvlion.wearlink.request.hasLocalNetworkAccessPermission
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ShortcutExecuteState {
  val title: String?
    get() = null

  data class Loading(
    override val title: String? = null
  ) : ShortcutExecuteState

  data class Success(
    override val title: String
  ) : ShortcutExecuteState

  data class Failure(
    override val title: String
  ) : ShortcutExecuteState

  data object RequestNotFound : ShortcutExecuteState
}

private val MINIMUM_LOADING_DURATION = 2.seconds

class ShortcutExecuteViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()

  private val _state = MutableStateFlow<ShortcutExecuteState>(ShortcutExecuteState.Loading())
  val state = _state.asStateFlow()

  private val _viewMode = MutableStateFlow(AppConstants.ViewMode.DEFAULT)
  val viewMode = _viewMode.asStateFlow()

  private var started = false

  init {
    viewModelScope.launch {
      dataStore.getViewType.collect { type ->
        _viewMode.value = AppConstants.ViewMode.entries.first { it.type == type }
      }
    }
  }

  fun execute(requestId: String?, getString: (Int) -> String) {
    if (started) return
    started = true

    viewModelScope.launch(Dispatchers.IO) {
      val savedRequestJson = dataStore.getSavedRequest.first()
      val request = if (requestId.isNullOrBlank() || savedRequestJson.isNullOrEmpty()) {
        null
      } else {
        savedRequestJson.parseRequestParams().findById(requestId)
      }

      if (request == null) {
        _state.value = ShortcutExecuteState.RequestNotFound
        return@launch
      }

      _state.value = ShortcutExecuteState.Loading(request.title)

      val responseDeferred = async {
        executeRequest(
          requester,
          request,
          isMobile = true,
          hasLocalNetworkPermission = getApplication<Application>().hasLocalNetworkAccessPermission(),
          getString = getString
        )
      }
      delay(MINIMUM_LOADING_DURATION)
      val response = responseDeferred.await()

      dataStore.appendResponse(response)

      _state.value = if (response.responseCode in 200..299) {
        ShortcutExecuteState.Success(response.title)
      } else {
        ShortcutExecuteState.Failure(response.title)
      }
    }
  }
}
