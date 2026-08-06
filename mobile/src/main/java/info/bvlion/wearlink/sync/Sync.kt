package info.bvlion.wearlink.sync

import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import info.bvlion.wearlink.request.WearMobileConnector
import kotlinx.coroutines.flow.first

object Sync {
  suspend fun requestsSyncToWear(dataStore: AppDataStore, wearConnector: WearMobileConnector) {
    val watchTargetRequests = dataStore.getSavedRequest.first()
      ?.parseRequestParams()
      ?.filter { it.watchSync || it.watchfaceShortcut }
      ?: emptyList()
    wearConnector.sendMessageToWear(
      WearMobileConnector.WEAR_SAVE_REQUEST_PATH,
      watchTargetRequests.toRequestParamsJson().toByteArray()
    )
  }
}