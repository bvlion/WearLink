package net.ambitious.android.httprequesttile.tile

import androidx.wear.tiles.ActionBuilders
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.httpexecute.HttpExecuteActivity

object ClickAction {
  fun requestExecute(requestParams: RequestParams): ActionBuilders.AndroidActivity =
    ActionBuilders.AndroidActivity.Builder()
      .setPackageName("net.ambitious.android.httprequesttile.debug")
      .setClassName("net.ambitious.android.httprequesttile.httpexecute.HttpExecuteActivity")
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