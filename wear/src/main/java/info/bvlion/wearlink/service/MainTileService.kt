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
import info.bvlion.appinfomanager.analytics.AnalyticsManager
import info.bvlion.wearlink.R
import info.bvlion.wearlink.analytics.AppAnalytics
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.request.WearMobileConnector
import info.bvlion.wearlink.tile.LinkTileRenderer
import info.bvlion.wearlink.tile.LinkTileState
import info.bvlion.wearlink.toast.ToastActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@SuppressLint("WearRecents")
class MainTileService : SuspendingTileService() {
  private val dataStore by lazy { AppDataStore.getDataStore(this) }

  private val savedRequest = MutableStateFlow<String?>("")

  private val render by lazy { LinkTileRenderer(this) }

  override fun onCreate() {
    super.onCreate()
    CoroutineScope(Dispatchers.IO).launch {
      dataStore.getSavedRequest.collect {
        savedRequest.value = it
      }
    }
  }

  override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources =
    render.produceRequestedResources(true, requestParams)

  override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    when (requestParams.currentState.lastClickableId) {
      AppConstants.START_MOBILE_ACTIVITY -> {
        AppConstants.startMobileActivity(
          this,
          successProcess = {
            startActivity(
              Intent(this, ToastActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, getString(R.string.tiles_toast_called_mobile))
            )
          }
        ) {
          showNotFindMobileToast()
        }
        AnalyticsManager.logEvent(AppAnalytics.EVENT_TILE_HEADER_TAP, null)
      }
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
                  showNotFindMobileToast()
                }
            }
          }
        ) {
          showNotFindMobileToast()
        }
        AnalyticsManager.logEvent(AppAnalytics.EVENT_TILE_SYNC_TAP, null)
      }
    }

    return render.renderTimeline(
      LinkTileState(
        savedRequest.value
          ?.takeIf { it.isNotEmpty() }
          ?.parseRequestParams()
          ?: listOf()
      ),
      requestParams
    )
  }

  private fun showNotFindMobileToast() =
    startActivity(
      Intent(this, ToastActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, getString(R.string.tiles_toast_not_found_mobile))
    )

  companion object {
    fun tileUpdate(context: Context) {
      getUpdater(context).requestUpdate(MainTileService::class.java)
    }
  }
}