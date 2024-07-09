package net.ambitious.android.httprequesttile.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.ambitious.android.httprequesttile.data.AppDataStore
import net.ambitious.android.httprequesttile.request.WearMobileConnector

class MobilesDataListenerService : WearableListenerService() {

  private val dataStore by lazy { AppDataStore.getDataStore(this) }

  private val job = SupervisorJob()
  private val scope = CoroutineScope(Dispatchers.IO + job)

  override fun onMessageReceived(messageEvent: MessageEvent) {
    super.onMessageReceived(messageEvent)
    when (messageEvent.path) {
      WearMobileConnector.WEAR_SAVE_REQUEST_PATH ->
        scope.launch {
          dataStore.saveRequest(String(messageEvent.data))
          MainTileService.tileUpdate(this@MobilesDataListenerService)
        }
      WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH ->
        scope.launch {
          WearMobileConnector(application).sendMessageToMobile(
            WearMobileConnector.MOBILE_SAVE_RESPONSE_PATH,
            dataStore.getSavedResponse.first().toByteArray()
          )
        }
      WearMobileConnector.WEAR_SAVED_RESPONSE_PATH ->
        scope.launch {
          dataStore.saveResponse("")
        }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    job.cancel()
  }
}