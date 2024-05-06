package net.ambitious.android.wearnetvoyage.service

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.ambitious.android.wearnetvoyage.analytics.AppAnalytics
import net.ambitious.android.wearnetvoyage.data.AppConstants
import net.ambitious.android.wearnetvoyage.data.AppDataStore
import net.ambitious.android.wearnetvoyage.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.wearnetvoyage.request.WearMobileConnector
import net.ambitious.android.wearnetvoyage.tile.LinkTileRenderer
import net.ambitious.android.wearnetvoyage.tile.LinkTileState
import net.ambitious.android.wearnetvoyage.toast.ToastActivity

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
  private val dataStore by lazy { AppDataStore.getDataStore(this) }

  private val savedRequest = MutableStateFlow<String?>("")

  private val render by lazy { LinkTileRenderer(this) }

  override fun onCreate() {
    super.onCreate()
    lifecycleScope.launch(Dispatchers.IO) {
      dataStore.getSavedRequest.collect {
        savedRequest.value = it
      }
    }
  }

  override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources =
    render.produceRequestedResources(true, requestParams)

  override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    when (requestParams.state?.lastClickableId) {
      AppConstants.START_MOBILE_ACTIVITY -> {
        AppConstants.startMobileActivity(
          this,
          lifecycleScope,
          successProcess = {
            startActivity(
              Intent(this, ToastActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "スマートフォンのアプリを呼び出します")
            )
          }
        ) {
          showNotFindMobileToast()
        }
        AppAnalytics.logEvent(AppAnalytics.EVENT_TILE_HEADER_TAP)
      }
      AppConstants.SYNC_STORE_DATA -> {
        AppConstants.startMobileActivity(
          this,
          lifecycleScope,
          "wearnetvoyage://sync",
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
                        .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "同期しました")
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
        AppAnalytics.logEvent(AppAnalytics.EVENT_TILE_SYNC_TAP)
      }
    }

    return render.renderTimeline(
      LinkTileState(
        savedRequest.value?.let {
          if (it.isEmpty()) {
            null
          } else {
            it.parseRequestParams()
          }
        } ?: listOf()
      ),
      requestParams
    )
  }

  private fun showNotFindMobileToast() =
    startActivity(
      Intent(this, ToastActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "スマートフォンが見つかりませんでした")
    )

  companion object {
    fun tileUpdate(context: Context) {
      getUpdater(context).requestUpdate(MainTileService::class.java)
    }
  }
}