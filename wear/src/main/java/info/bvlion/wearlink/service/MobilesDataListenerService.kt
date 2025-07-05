package info.bvlion.wearlink.service

import android.content.ComponentName
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.request.WearMobileConnector

class MobilesDataListenerService : WearableListenerService() {

  private val dataStore by lazy { AppDataStore.getDataStore(this) }
  private val connector by lazy { WearMobileConnector(this) }

  private val job = SupervisorJob()
  private val scope = CoroutineScope(Dispatchers.IO + job)

  override fun onMessageReceived(messageEvent: MessageEvent) {
    super.onMessageReceived(messageEvent)
    when (messageEvent.path) {
      WearMobileConnector.WEAR_SAVE_REQUEST_PATH ->
        scope.launch {
          val requestData = String(messageEvent.data)
          dataStore.saveRequest(requestData)
          MainTileService.tileUpdate(this@MobilesDataListenerService)
          if (requestData.parseRequestParams().any { it.watchfaceShortcut }) {
            ComplicationDataSourceUpdateRequester.create(
              this@MobilesDataListenerService,
              ComponentName(
                this@MobilesDataListenerService,
                ComplicationService::class.java
              )
            ).requestUpdateAll()
          }
        }
      WearMobileConnector.WEAR_REQUEST_RESPONSE_PATH ->
        scope.launch {
          connector.sendMessageToMobile(
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
