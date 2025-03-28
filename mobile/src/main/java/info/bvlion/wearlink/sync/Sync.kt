package info.bvlion.wearlink.sync

import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.request.WearMobileConnector
import kotlinx.coroutines.flow.first
import org.json.JSONArray

object Sync {
  suspend fun requestsSyncToWear(dataStore: AppDataStore, wearConnector: WearMobileConnector) {
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
    wearConnector.sendMessageToWear(WearMobileConnector.WEAR_SAVE_REQUEST_PATH, watchSavedRequests)
  }
}