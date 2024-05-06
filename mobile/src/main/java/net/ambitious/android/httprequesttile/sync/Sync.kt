package net.ambitious.android.wearnetvoyage.sync

import kotlinx.coroutines.flow.first
import net.ambitious.android.wearnetvoyage.data.AppDataStore
import net.ambitious.android.wearnetvoyage.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.wearnetvoyage.request.WearMobileConnector
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