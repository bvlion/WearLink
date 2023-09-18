package net.ambitious.android.httprequesttile.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.remote.interactions.RemoteActivityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

object AppConstants {
  const val START_MOBILE_ACTIVITY = "net.ambitious.android.httprequesttile.START_MOBILE_ACTIVITY"
  const val SYNC_STORE_DATA = "net.ambitious.android.httprequesttile.SYNC_STORE_DATA"

  fun startMobileActivity(
    context: Context,
    scope: CoroutineScope,
    url: String = "httprequesttile://start",
    errorProcess: () -> Unit = {}
  ) {
    val remoteActivityHelper = RemoteActivityHelper(context)
    scope.launch {
      try {
        remoteActivityHelper.startRemoteActivity(
          Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(url))
        ).await()
      } catch (e: Exception) {
        errorProcess()
      }
    }
  }
}
