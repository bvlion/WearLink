package info.bvlion.wearlink.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.httpexecute.HttpExecuteActivity

object ClickAction {
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