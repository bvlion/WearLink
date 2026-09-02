package info.bvlion.wearlink.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import info.bvlion.wearlink.wear.R
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.request.WearMobileConnector
import info.bvlion.wearlink.tile.LinkTileRenderer
import info.bvlion.wearlink.tile.LinkTileState
import info.bvlion.wearlink.toast.ToastActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@SuppressLint("WearRecents")
class MainTileService : SuspendingTileService() {
  private val dataStore by lazy { AppDataStore.getDataStore(this) }

  private val render by lazy { LinkTileRenderer(this) }

  override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources =
    render.produceRequestedResources(true, requestParams)

  override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    when (requestParams.currentState.lastClickableId) {
      AppConstants.SYNC_STORE_DATA -> {
        AppConstants.startMobileActivity(
          this,
          "wearlink://sync",
          {
            lifecycleScope.launch(Dispatchers.IO) {
              delay(500)
              WearMobileConnector(this@MainTileService)
                .sendMessageToMobile(
                  WearMobileConnector.MOBILE_REQUEST_SYNC_PATH,
                  successProcess = {
                    startActivity(
                      Intent(this@MainTileService, ToastActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, getString(R.string.tiles_toast_synced))
                    )
                  }
                ) {
                  showSyncFailedToast()
                }
            }
          }
        ) {
          showSyncFailedToast()
        }
      }
    }

    return render.renderTimeline(
      LinkTileState(
        dataStore.getSavedRequest.first()
          ?.takeIf { it.isNotEmpty() }
          ?.parseRequestParams()
          ?.filter { it.watchSync }
          ?: listOf()
      ),
      requestParams
    )
  }

  private fun showSyncFailedToast() =
    startActivity(
      Intent(this, ToastActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, getString(R.string.tiles_toast_sync_failed))
    )

  companion object {
    fun tileUpdate(context: Context) {
      getUpdater(context).requestUpdate(MainTileService::class.java)
    }
  }
}
