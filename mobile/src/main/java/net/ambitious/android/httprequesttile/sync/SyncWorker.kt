package net.ambitious.android.wearnetvoyage.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import net.ambitious.android.wearnetvoyage.data.AppDataStore
import net.ambitious.android.wearnetvoyage.request.WearMobileConnector

class SyncWorker(
  private val context: Context, params: WorkerParameters
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val dataStore = AppDataStore.getDataStore(context)
    val wearConnector = WearMobileConnector(context)
    Sync.requestsSyncToWear(dataStore, wearConnector)
    return Result.success()
  }
}