package info.bvlion.wearlink.data

import android.content.Context
import android.content.Intent
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.core.net.toUri

object AppConstants {
  const val START_MOBILE_ACTIVITY = "info.bvlion.START_MOBILE_ACTIVITY"
  const val SYNC_STORE_DATA = "info.bvlion.SYNC_STORE_DATA"

  fun startMobileActivity(
    context: Context,
    url: String = "wearlink://start",
    successProcess: () -> Unit,
    errorProcess: () -> Unit = {}
  ) {
    val future = RemoteActivityHelper(context).startRemoteActivity(
      Intent(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(url.toUri())
    )
    future.addListener({
      try {
        future.get()
        successProcess()
      } catch (e: Exception) {
        errorProcess()
      }
    }, context.mainExecutor)
  }
}