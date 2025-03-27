package info.bvlion.wearlink.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.request.WearMobileConnector

class SyncWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val dataStore = AppDataStore.getDataStore(context)
    val wearConnector = WearMobileConnector(context)
    Sync.requestsSyncToWear(dataStore, wearConnector)
    return Result.success()
  }
}