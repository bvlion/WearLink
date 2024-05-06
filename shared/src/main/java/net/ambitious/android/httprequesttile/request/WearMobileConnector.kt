package net.ambitious.android.wearnetvoyage.request

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class WearMobileConnector(context: Context) {

  private val messageClient by lazy { Wearable.getMessageClient(context) }
  private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }

  suspend fun sendMessageToMobile(
    path: String,
    data: ByteArray = byteArrayOf(),
    successProcess: () -> Unit = {},
    errorProcess: (Exception) -> Unit = {}
  ) = sendMessage(MOBILE_CAPABILITY, path, data, successProcess, errorProcess)

  suspend fun sendMessageToWear(
    path: String,
    data: ByteArray = byteArrayOf(),
    successProcess: () -> Unit = {},
    errorProcess: (Exception) -> Unit = {}
  ) = sendMessage(WEAR_CAPABILITY, path, data, successProcess, errorProcess)

  private suspend fun sendMessage(
    capability: String,
    path: String,
    data: ByteArray,
    successProcess: () -> Unit,
    errorProcess: (Exception) -> Unit
  ) = coroutineScope {
    try {
      val nodes = capabilityClient
        .getCapability(capability, CapabilityClient.FILTER_REACHABLE)
        .await()
        .nodes

      if (nodes.isEmpty()) {
        throw Exception("No node was found")
      }

      nodes.map { node ->
        async {
          messageClient.sendMessage(node.id, path, data).await()
        }
      }.awaitAll()
      successProcess()
    } catch (exception: Exception) {
      FirebaseCrashlytics.getInstance().recordException(exception)
      errorProcess(exception)
    }
  }

  companion object {
    private const val WEAR_CAPABILITY = "wear"
    private const val MOBILE_CAPABILITY = "mobile"

    /** Wear に Request 一覧を送る */
    const val WEAR_SAVE_REQUEST_PATH = "/save-request-params"
    /** Wear に Response 一覧を送るように依頼する */
    const val WEAR_REQUEST_RESPONSE_PATH = "/request-to-send-responses"
    /** Wear から送られた Response 一覧が保存できたことを通知する */
    const val WEAR_SAVED_RESPONSE_PATH = "/saved-send-responses"

    /** Mobile に　Response 一覧を送る */
    const val MOBILE_SAVE_RESPONSE_PATH = "/save-wear-responses"
    /** Mobile に　同期を解するように依頼する */
    const val MOBILE_REQUEST_SYNC_PATH = "/request-sync"
  }
}