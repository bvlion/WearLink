package info.bvlion.wearlink

import android.app.Application
import android.content.ClipData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.appinfomanager.analytics.AnalyticsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import info.bvlion.wearlink.analytics.AppAnalytics
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.ErrorDetail
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.request.HttpRequester
import info.bvlion.wearlink.request.WearMobileConnector
import info.bvlion.wearlink.sync.Sync
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONArray
import java.util.Date

class MobileMainViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()
  private val wearConnector = WearMobileConnector(application)

  private val _savedRequest = MutableStateFlow<String?>("")
  val savedRequest = _savedRequest.asStateFlow()

  private val _savedResponse = MutableStateFlow("")
  val savedResponse = _savedResponse.asStateFlow()

  private val _errorDialog = MutableStateFlow<ErrorDetail?>(null)
  val errorDialog = _errorDialog.asStateFlow()

  private val _loading = MutableStateFlow(false)
  val loading = _loading.asStateFlow()

  private val _viewMode = MutableStateFlow(AppConstants.ViewMode.DEFAULT)
  val viewMode = _viewMode.asStateFlow()

  private val _snackbar = Channel<String>(Channel.BUFFERED)
  val snackbar = _snackbar.receiveAsFlow()

  private val _clipboard = Channel<ClipData>(Channel.BUFFERED)
  val clipboard = _clipboard.receiveAsFlow()

  private val _firstSendAnalytics = MutableStateFlow(false)

  init {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedRequest.collect { value ->
        _savedRequest.value = value
        Sync.requestsSyncToWear(dataStore, wearConnector)
        if (!_firstSendAnalytics.value && !value.isNullOrEmpty()) {
          _firstSendAnalytics.value = true
          AnalyticsManager.logEvent(
            AppAnalytics.EVENT_START,
            mapOf(AppAnalytics.PARAM_EVENT_START_REQUEST_SAVED_COUNT to value.parseRequestParams().size.toString())
          )
        }
      }
    }
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedResponse.collect {
        _savedResponse.value = it
      }
    }
    viewModelScope.launch {
      dataStore.getViewType.collect { type ->
        _viewMode.value = AppConstants.ViewMode.entries.first { it.type == type }
      }
    }
  }

  fun requestResponsesToWear() {
    viewModelScope.launch(Dispatchers.IO) {
      wearConnector.sendMessageToWear(WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH)
    }
  }

  fun saveWearResponses(responses: String) {
    viewModelScope.launch(Dispatchers.IO) {
      responses.parseResponseParams().forEach {
        saveResponses(it)
      }
      wearConnector.sendMessageToWear(WearMobileConnector.WEAR_SAVED_RESPONSE_PATH)
    }
  }

  fun saveRequest(
    savedIndex: Int,
    request: RequestParams,
    getString: ((Int, String) -> String)?
  ) {
    viewModelScope.launch(Dispatchers.IO) {
        (savedRequest.value?.parseRequestParams()?.toMutableList() ?: mutableListOf())
        .map {
          if (request.watchfaceShortcut) {
            it.copy(watchfaceShortcut = false)
          } else {
            it
          }
        }
        .toMutableList()
        .apply {
          if (savedIndex >= 0) {
            set(savedIndex, request)
          } else {
            add(0, request)
          }
        }
        .map { it.toJsonString() }
        .let { JSONArray(it).toString() }
        .let { dataStore.saveRequest(it) }
      AnalyticsManager.logEvent(
        AppAnalytics.EVENT_REQUEST_SAVE_TAP,
        mapOf(AppAnalytics.PARAM_EVENT_REQUEST_SAVE_COUNT to savedRequest.value?.parseRequestParams()?.size.toString())
      )
    }
    getString?.invoke(
      if (savedIndex >= 0)
        R.string.request_updated
      else
        R.string.request_created,
      request.title
    )?.let { showSnackbar(it) }
  }

  fun deleteRequest(deleteIndex: Int, getString: (Int) -> String) {
    viewModelScope.launch {
      savedRequest.value?.run {
        parseRequestParams()
          .toMutableList()
          .apply {
            removeAt(deleteIndex)
          }
          .map { it.toJsonString() }
          .let { JSONArray(it).toString() }
          .let { dataStore.saveRequest(it) }
      }
    }
    showSnackbar(getString(R.string.request_deleted))
  }

  fun sendRequest(request: RequestParams, getString: (Int) -> String) {
    _loading.value = true
    val start = System.currentTimeMillis()
    viewModelScope.launch(Dispatchers.IO) {
      val response = try {
        requester.execute(request)
      } catch (e: Exception) {
        ResponseParams(
          request.title,
          -1,
          System.currentTimeMillis() - start,
          "",
          "${getString(info.bvlion.wearlink.shared.R.string.request_error)}\n${e.message}",
          Date().time,
          true
        )
      }
      _loading.value = false
      showSnackbar(getString(R.string.request_sent))
      saveResponses(response)
    }
  }

  private suspend fun saveResponses(response: ResponseParams) {
    val savedList = if (savedResponse.value.isBlank()) {
      mutableListOf()
    } else {
      savedResponse.value.parseResponseParams().toMutableList()
    }
    savedList
      .apply { add(response) }
      .sortedByDescending { it.sendDateTime }
      .map { it.toJsonString() }
      .let { JSONArray(it).toString() }
      .let { dataStore.saveResponse(it) }
    AnalyticsManager.logEvent(
      AppAnalytics.EVENT_RESPONSE_SAVE_TAP,
      mapOf(AppAnalytics.PARAM_EVENT_RESPONSE_SAVE_COUNT to savedList.size.toString())
    )
  }

  fun copyToClipboard(isRequestCopy: Boolean, getString: (Int) -> String) {
    val clipData = if (isRequestCopy) {
      savedRequest.value?.let { value ->
        ClipData.newPlainText(
          "request",
          value.parseRequestParams().joinToString(",", "[", "]") { it.toJsonString() }
        )
      }
    } else {
      if (savedResponse.value.isNotEmpty()) {
        ClipData.newPlainText(
          "response",
          savedResponse.value.parseResponseParams()
            .joinToString(",", "[", "]") { it.toJsonString() }
        )
      } else {
        null
      }
    }
    clipData?.let {
      viewModelScope.launch {
        _clipboard.send(it)
      }
    }
    showSnackbar(getString(if (clipData != null) {
      R.string.copied_clipboard
    } else {
      R.string.copied_clipboard_error
    }))
  }

  fun saveRequest(json: String, getString: (Int) -> String) {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.saveRequest(json)
    }
    showSnackbar(getString(R.string.request_imported))
  }

  fun deleteResponses(getString: (Int) -> String) {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.saveResponse("")
    }
    showSnackbar(getString(R.string.execute_history_deleted))
  }

  fun saveViewMode(viewMode: AppConstants.ViewMode) {
    _viewMode.value = viewMode
    viewModelScope.launch {
      dataStore.setViewType(viewMode.type)
    }
  }

  fun dismissErrorDialog() {
    _errorDialog.value = null
  }

  fun syncWatch(getString: (Int) -> String) {
    viewModelScope.launch(Dispatchers.IO) {
      Sync.requestsSyncToWear(dataStore, wearConnector)
      wearConnector.sendMessageToWear(
        WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH,
        successProcess = {
          showSnackbar(getString(R.string.sync_wearable))
        }
      ) {
        showSnackbar(getString(R.string.sync_wearable_error))
      }
    }
  }

  fun showWatchSyncError(title: String, message: String) {
    _errorDialog.value = ErrorDetail(title, message)
  }

  private fun showSnackbar(message: String) {
    viewModelScope.launch {
      _snackbar.send(message)
    }
  }
}