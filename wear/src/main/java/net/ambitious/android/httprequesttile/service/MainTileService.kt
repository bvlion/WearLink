package net.ambitious.android.httprequesttile.service

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
import net.ambitious.android.httprequesttile.analytics.AppAnalytics
import net.ambitious.android.httprequesttile.data.AppConstants
import net.ambitious.android.httprequesttile.data.AppDataStore
import net.ambitious.android.httprequesttile.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.httprequesttile.request.WearMobileConnector
import net.ambitious.android.httprequesttile.tile.LinkTileRenderer
import net.ambitious.android.httprequesttile.tile.LinkTileState
import net.ambitious.android.httprequesttile.toast.ToastActivity

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
  private val dataStore by lazy { AppDataStore.getDataStore(this) }

  private val savedRequest = MutableStateFlow<String?>("")

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
        AppConstants.startMobileActivity(this, lifecycleScope) {
          startActivity(
            Intent(this, ToastActivity::class.java)
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "スマートフォンのアプリがインストールされていません")
          )
        }
        startActivity(
          Intent(this, ToastActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "スマートフォンのアプリを呼び出します")
        )
        AppAnalytics.logEvent(AppAnalytics.EVENT_TILE_HEADER_CLICK)
      }
      AppConstants.SYNC_STORE_DATA -> {
        lifecycleScope.launch(Dispatchers.IO) {
          AppConstants.startMobileActivity(this@MainTileService, lifecycleScope) { }
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
              startActivity(
                Intent(this@MainTileService, ToastActivity::class.java)
                  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  .putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, "スマートフォンが見つかりませんでした")
              )
            }
        }
        AppAnalytics.logEvent(AppAnalytics.EVENT_TILE_SYNC_CLICK)
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

  private val render by lazy { LinkTileRenderer(this) }

  companion object {
    fun tileUpdate(context: Context) {
      getUpdater(context).requestUpdate(MainTileService::class.java)
    }
  }
}