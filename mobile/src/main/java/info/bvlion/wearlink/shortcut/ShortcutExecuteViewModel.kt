package info.bvlion.wearlink.shortcut

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import info.bvlion.wearlink.request.HttpRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

internal fun resolveRequest(requestId: String?, savedRequestJson: String?): RequestParams? {
  if (requestId.isNullOrBlank() || savedRequestJson.isNullOrEmpty()) {
    return null
  }
  return savedRequestJson.parseRequestParams().findById(requestId)
}

internal fun outcomeFor(response: ResponseParams): ShortcutExecuteState =
  if (response.responseCode in 200..299) {
    ShortcutExecuteState.Success(response.title)
  } else {
    ShortcutExecuteState.Failure(response.title)
  }

internal suspend fun awaitAtLeast(
  minimumDelayMillis: Long,
  block: suspend () -> ResponseParams
): ResponseParams = coroutineScope {
  val resultDeferred = async(Dispatchers.IO) { block() }
  val timerDeferred = async(Dispatchers.IO) { delay(minimumDelayMillis) }
  listOf(resultDeferred, timerDeferred).awaitAll().filterIsInstance<ResponseParams>().first()
}

/**
 * Resolves the request, runs [executeHttp] only when it is found, waits for the slower of
 * [executeHttp] and [minimumLoadingMillis], saves the resulting response via [saveResponse], and
 * returns the terminal [ShortcutExecuteState]. Kept free of Android dependencies so the whole
 * flow can be unit tested with fake [executeHttp]/[saveResponse] implementations.
 */
internal suspend fun runShortcutExecution(
  requestId: String?,
  savedRequestJson: String?,
  minimumLoadingMillis: Long = MINIMUM_LOADING_MILLIS,
  onLoading: (RequestParams) -> Unit = {},
  executeHttp: suspend (RequestParams) -> ResponseParams,
  saveResponse: suspend (ResponseParams) -> Unit
): ShortcutExecuteState {
  val request = resolveRequest(requestId, savedRequestJson)
    ?: return ShortcutExecuteState.RequestNotFound

  onLoading(request)

  val response = awaitAtLeast(minimumLoadingMillis) { executeHttp(request) }
  saveResponse(response)
  return outcomeFor(response)
}

/** Guards a single logical execution against being started more than once, e.g. on Activity recreation. */
internal class SingleExecutionGuard {
  private var started = false

  @Synchronized
  fun tryStart(): Boolean {
    if (started) return false
    started = true
    return true
  }
}

class ShortcutExecuteViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()

  private val _state = MutableStateFlow<ShortcutExecuteState>(ShortcutExecuteState.Loading())
  val state = _state.asStateFlow()

  private val executionGuard = SingleExecutionGuard()

  fun execute(requestId: String?, getString: (Int) -> String) {
    if (!executionGuard.tryStart()) return

    viewModelScope.launch(Dispatchers.IO) {
      val result = runShortcutExecution(
        requestId = requestId,
        savedRequestJson = dataStore.getSavedRequest.first(),
        onLoading = { _state.value = ShortcutExecuteState.Loading(it.title) },
        executeHttp = { request -> executeHttpRequest(request, getString) },
        saveResponse = { saveResponse(it) }
      )
      _state.value = result
    }
  }

  private suspend fun executeHttpRequest(request: RequestParams, getString: (Int) -> String): ResponseParams {
    val start = System.currentTimeMillis()
    return try {
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
