package info.bvlion.wearlink.request

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.util.Date

class RequestExecutor(private val context: Context) {
  private val requester = HttpRequester()
  private val dataStore = AppDataStore.getDataStore(context)

  suspend fun execute(request: RequestParams, getString: (Int) -> String): ResponseParams {
    val start = System.currentTimeMillis()
    val response = try {
      requester.execute(request)
    } catch (e: Exception) {
      val localNetworkPermissionGuidance = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
        ContextCompat.checkSelfPermission(
          context,
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
    saveResponse(response)
    return response
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
