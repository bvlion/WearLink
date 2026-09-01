package info.bvlion.wearlink.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import info.bvlion.wearlink.WearMainActivity
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.httpexecute.HttpExecuteActivity

object ClickAction {
  /** スマホ版の起動可否と案内は WearMainActivity へ集約するため、Tile からはメイン画面を開く */
  fun openMainActivity(context: Context): ActionBuilders.AndroidActivity =
    ActionBuilders.AndroidActivity.Builder()
      .setPackageName(context.packageName)
      .setClassName(WearMainActivity::class.java.name)
      .build()

  fun requestExecute(
    context: Context,
    requestParams: RequestParams
  ): ActionBuilders.AndroidActivity =
    ActionBuilders.AndroidActivity.Builder()
      .setPackageName(context.packageName)
      .setClassName(HttpExecuteActivity::class.java.name)
      .addKeyToExtraMapping(
        HttpExecuteActivity.EXTRA_REQUEST_PARAMS,
        ActionBuilders.stringExtra(requestParams.toJsonString())
      )
      .addKeyToExtraMapping(
        HttpExecuteActivity.EXTRA_REQUEST_TITLE,
        ActionBuilders.stringExtra(requestParams.title)
      )
      .build()
}