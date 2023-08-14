package net.ambitious.android.httprequesttile

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScaffoldState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.ambitious.android.httprequesttile.data.AppConstants
import net.ambitious.android.httprequesttile.data.AppDataStore
import net.ambitious.android.httprequesttile.data.Constant
import net.ambitious.android.httprequesttile.data.ErrorDetail
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.httprequesttile.data.ResponseParams
import net.ambitious.android.httprequesttile.data.ResponseParams.Companion.parseResponseParams
import net.ambitious.android.httprequesttile.request.HttpRequester
import net.ambitious.android.httprequesttile.request.WearMobileConnector
import org.json.JSONArray
import java.util.Date

@ExperimentalMaterialApi
class MobileMainViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()
  private val wearConnector = WearMobileConnector(application)

  val resultBottomSheet = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)

  private val _savedRequest = MutableStateFlow<String?>("")
  val savedRequest = _savedRequest.asStateFlow()

  private val _savedResponse = MutableStateFlow("")
  val savedResponse = _savedResponse.asStateFlow()

  private val _errorDialog = MutableStateFlow<ErrorDetail?>(null)
  val errorDialog = _errorDialog.asStateFlow()

  private val _loading = MutableStateFlow(false)
  val loading = _loading.asStateFlow()

  private val _rules = MutableStateFlow("")
  val rules = _rules.asStateFlow()

  private val _viewMode = MutableStateFlow(AppConstants.ViewMode.DEFAULT)
  val viewMode = _viewMode.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedRequest.collect { value ->
        _savedRequest.value = value
        requestsSyncToWear()
      }
    }
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedResponse.collect {
        _savedResponse.value = it
      }
    }
    viewModelScope.launch {
      dataStore.getViewType.collect { type ->
        _viewMode.value = AppConstants.ViewMode.values().first { it.type == type }
      }
    }
  }

  private suspend fun requestsSyncToWear() {
    val watchSavedRequests = dataStore.getSavedRequest.first()?.let { value ->
      value.parseRequestParams()
        .filter { it.watchSync }
        .map { it.toJsonString() }
        .let {
          if (it.isEmpty()) {
            byteArrayOf()
          } else {
            JSONArray(it).toString().toByteArray()
          }
        }
    } ?: byteArrayOf()
    wearConnector.sendMessageToWear(WearMobileConnector.WEAR_SAVE_REQUEST_PATH, watchSavedRequests) {
      // TODO Crashlytics
    }
  }

  fun requestResponsesToWear() {
    viewModelScope.launch(Dispatchers.IO) {
      wearConnector.sendMessageToWear(WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH) {
        // TODO Crashlytics
      }
    }
  }

  fun saveWearResponses(responses: String) {
    viewModelScope.launch(Dispatchers.IO) {
      responses.parseResponseParams().forEach {
        saveResponses(it)
      }
      wearConnector.sendMessageToWear(WearMobileConnector.WEAR_SAVED_RESPONSE_PATH) {
        // TODO Crashlytics
      }
    }
  }

  fun saveRequest(scope: CoroutineScope?, scaffoldState: ScaffoldState?, savedIndex: Int, request: RequestParams) {
    viewModelScope.launch(Dispatchers.IO) {
      (savedRequest.value?.parseRequestParams()?.toMutableList() ?: mutableListOf())
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
    }
    if (scope != null && scaffoldState != null) {
      showMessageSnackbar(scope, scaffoldState, "「${request.title}」を${if (savedIndex >= 0) "更新" else "作成"}しました。")
    }
  }

  fun deleteRequest(scope: CoroutineScope, scaffoldState: ScaffoldState, deleteIndex: Int) {
    viewModelScope.launch(Dispatchers.IO) {
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
    showMessageSnackbar(scope, scaffoldState, "削除しました。")
  }

  fun sendRequest(scope: CoroutineScope, scaffoldState: ScaffoldState, request: RequestParams) {
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
          "通信エラーが発生しました。\n${e.message}",
          Date().time,
          true
        )
      }
      _loading.value = false
      showMessageSnackbar(scope, scaffoldState, "送信しました。")
      saveResponses(response)
    }
  }

  private suspend fun saveResponses(response: ResponseParams) {
    if (savedResponse.value.isBlank()) {
      mutableListOf()
    } else {
      savedResponse.value.parseResponseParams().toMutableList()
    }
      .apply { add(response) }
      .sortedByDescending { it.sendDateTime }
      .map { it.toJsonString() }
      .let { JSONArray(it).toString() }
      .let { dataStore.saveResponse(it) }
  }

  fun hideBottomSheet(scope: CoroutineScope) {
    scope.launch {
      resultBottomSheet.hide()
    }
  }

  private fun showMessageSnackbar(scope: CoroutineScope, scaffoldState: ScaffoldState, message: String) {
    scope.launch {
      scaffoldState.snackbarHostState.showSnackbar(message)
    }
  }

  fun showRules(url: String) {
    _rules.value = url
  }

  fun dismissRules() {
    _rules.value = ""
  }

  fun copyToClipboard(clipboardManager: ClipboardManager, scope: CoroutineScope, scaffoldState: ScaffoldState, isRequestCopy: Boolean) {
    val clipData = if (isRequestCopy) {
      ClipData.newPlainText("request", savedRequest.value)
    } else {
      ClipData.newPlainText("response", savedResponse.value)
    }
    scope.launch {
      clipboardManager.setPrimaryClip(clipData)
    }
    scope.launch {
      scaffoldState.snackbarHostState.showSnackbar("クリップボードにコピーしました。")
    }
  }

  fun saveRequest(scope: CoroutineScope, scaffoldState: ScaffoldState, json: String) {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.saveRequest(json)
    }
    showMessageSnackbar(scope, scaffoldState, "インポートしました。")
  }

  fun deleteResponses(scope: CoroutineScope, scaffoldState: ScaffoldState) {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.saveResponse("")
    }
    showMessageSnackbar(scope, scaffoldState, "実行履歴を削除しました。")
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

  fun syncWatch(scope: CoroutineScope?, scaffoldState: ScaffoldState?) {
    viewModelScope.launch(Dispatchers.IO) {
      requestsSyncToWear()
      wearConnector.sendMessageToWear(
        WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH,
        successProcess = {
          if (scope != null && scaffoldState != null) {
            showMessageSnackbar(scope, scaffoldState, "ウェアラブルと同期しました。")
          }
        }
      ) {
        // TODO Crashlytics
        if (scope != null && scaffoldState != null) {
          showMessageSnackbar(scope, scaffoldState, "ウェアラブルが見つかりません")
        }
      }
    }
  }

  fun showWatchSyncError() {
    _errorDialog.value = ErrorDetail(
      "ウェアラブル同期エラー",
      "同期できる通信は${Constant.MAX_SYNC_COUNT}つまでです。\n別の同期されている通信のチェックを外してください。"
    )
  }
}