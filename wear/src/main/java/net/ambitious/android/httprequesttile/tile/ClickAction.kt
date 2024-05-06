package net.ambitious.android.wearnetvoyage.tile

import android.content.Context
import androidx.wear.tiles.ActionBuilders
import net.ambitious.android.wearnetvoyage.data.RequestParams
import net.ambitious.android.wearnetvoyage.httpexecute.HttpExecuteActivity

object ClickAction {
  fun requestExecute(
    context: Context,
    requestParams: RequestParams
  ): ActionBuilders.AndroidActivity =
    ActionBuilders.AndroidActivity.Builder()
      .setPackageName(context.packageName)
      .setClassName("net.ambitious.android.wearnetvoyage.httpexecute.HttpExecuteActivity")
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