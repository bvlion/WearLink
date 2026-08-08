package info.bvlion.wearlink.shortcut

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import info.bvlion.wearlink.request.HttpRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date

sealed interface ShortcutExecuteState {
  data class Loading(val title: String? = null) : ShortcutExecuteState
  data class Success(val title: String) : ShortcutExecuteState
  data class Failure(val title: String) : ShortcutExecuteState
  data object RequestNotFound : ShortcutExecuteState
}

private const val MINIMUM_LOADING_MILLIS = 2000L

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

      val networkDeferred = async(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
          requester.execute(request)
        } catch (e: Exception) {
          val localNetworkPermissionGuidance = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            ContextCompat.checkSelfPermission(
              getApplication(),
              Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
          ) {
            "\n${getString(info.bvlion.wearlink.shared.R.string.local_network_permission_guidance)}"
          } else {
            ""
          }
          ResponseParams(
            request.title,
            -1,
            System.currentTimeMillis() - start,
            "",
            "${getString(info.bvlion.wearlink.shared.R.string.request_error)}\n${e.message}" +
              localNetworkPermissionGuidance,
            Date().time,
            true
          )
        }
      }
      val timerDeferred = async(Dispatchers.IO) {
        delay(MINIMUM_LOADING_MILLIS)
      }

      val response = listOf(networkDeferred, timerDeferred).awaitAll()
        .filterIsInstance<ResponseParams>()
        .first()

      saveResponse(response)

      _state.value = if (response.responseCode in 200..299) {
        ShortcutExecuteState.Success(response.title)
      } else {
        ShortcutExecuteState.Failure(response.title)
      }
    }
  }

  private suspend fun saveResponse(response: ResponseParams) {
    val savedResponse = dataStore.getSavedResponse.first()
    val savedList = if (savedResponse.isBlank()) {
      mutableListOf()
    } else {
      savedResponse.parseResponseParams().toMutableList()
    }
    savedList
      .apply { add(response) }
      .sortedByDescending { it.sendDateTime }
      .map { it.toJsonString() }
      .let { JSONArray(it).toString() }
      .let { dataStore.saveResponse(it) }
  }
}
