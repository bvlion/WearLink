package info.bvlion.wearlink.httpexecute

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import info.bvlion.wearlink.request.HttpRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date

class HttpExecuteViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()

  private val _savedResponse = MutableStateFlow("")

  private val _isSent = MutableStateFlow(false)
  val isSent = _isSent.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedResponse.collect {
        _savedResponse.value = it
      }
    }
  }

  fun sendRequest(param: String?, getString: (Int) -> String) {
    if (param.isNullOrEmpty()) {
      _isSent.value = true
      return
    }

    val request = param.parseRequestParam()
    val start = System.currentTimeMillis()
    viewModelScope.launch(Dispatchers.IO) {
      val networkDeferred = async(Dispatchers.IO) {
        try {
          requester.execute(request, false)
        } catch (e: Exception) {
          val localNetworkPermissionGuidance = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            ContextCompat.checkSelfPermission(
              getApplication<Application>(),
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
            false
          )
        }
      }
      val timerDeferred = async(Dispatchers.IO) {
        delay(2000)
      }

      val response = listOf(networkDeferred, timerDeferred).awaitAll()
        .filterIsInstance<ResponseParams>()
        .first()

      if (_savedResponse.value.isBlank()) {
        mutableListOf()
      } else {
        _savedResponse.value.parseResponseParams().toMutableList()
      }
        .apply { add(response) }
        .sortedByDescending { it.sendDateTime }
        .map { it.toJsonString() }
        .let { JSONArray(it).toString() }
        .let { dataStore.saveResponse(it) }
      _isSent.value = true
    }
  }
}
