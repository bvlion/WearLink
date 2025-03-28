package info.bvlion.wearlink.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import info.bvlion.appinfomanager.analytics.AnalyticsManager
import info.bvlion.wearlink.wear.R
import info.bvlion.wearlink.analytics.AppAnalytics
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.ui.theme.tilesColors

@OptIn(ExperimentalHorologistApi::class)
class LinkTileRenderer(context: Context) : SingleTileLayoutRenderer<LinkTileState, Boolean>(context) {
  override fun renderTile(
    state: LinkTileState,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
  ): LayoutElementBuilders.LayoutElement =
    linkTileLayout(
      context = context,
      deviceParameters = deviceParameters,
      state = state,
      requestClickableFactory = { requestParams ->
        AnalyticsManager.logEvent(
          AppAnalytics.EVENT_TILE_REQUEST_TAP,
          mapOf(AppAnalytics.PARAM_EVENT_TILE_REQUEST_TITLE_HASH to requestParams.title.hashCode().toString())
        )
        ModifiersBuilders.Clickable.Builder()
          .setId(requestParams.title)
          .setOnClick(
            ActionBuilders.LaunchAction.Builder()
              .setAndroidActivity(ClickAction.requestExecute(context, requestParams))
              .build()
          )
          .build()
      }
    )
}

private fun linkTileLayout(
  context: Context,
  deviceParameters: DeviceParametersBuilders.DeviceParameters,
  state: LinkTileState,
  requestClickableFactory: (RequestParams) -> ModifiersBuilders.Clickable,
) = PrimaryLayout.Builder(deviceParameters)
  .setResponsiveContentInsetEnabled(true)
  .setPrimaryLabelTextContent(
    Text.Builder(context, context.getString(info.bvlion.wearlink.shared.R.string.app_name))
      .setModifiers(
        ModifiersBuilders.Modifiers.Builder().setClickable(
          ModifiersBuilders.Clickable.Builder()
            .setId(AppConstants.START_MOBILE_ACTIVITY)
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build()
        )
          .build()
      )
      .setTypography(Typography.TYPOGRAPHY_CAPTION2)
      .setColor(argb(tilesColors.primary))
      .build()
  )
  .setContent(
    LinkContentLayout.create(
      context,
      state,
      deviceParameters,
      requestClickableFactory
    )
  )
  .setPrimaryChipContent(
    CompactChip.Builder(
      context,
      context.getString(R.string.tile_sync_button),
      ModifiersBuilders.Clickable.Builder()
        .setId(AppConstants.SYNC_STORE_DATA)
        .setOnClick(ActionBuilders.LoadAction.Builder().build())
        .build(),
      deviceParameters
    )
      .setChipColors(ChipColors.primaryChipColors(tilesColors))
      .build()
  )
  .build()

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun LinkTileRendererPreview() {
  val titles = listOf("玄関の鍵", "居間ｴｱｺﾝ", "寝る準備", "外出")
  val list = titles.map {
    RequestParams(
      url = "https://www.google.com/",
      title = it,
      method = Constant.HttpMethod.GET,
      bodyType = Constant.BodyType.QUERY,
      headers = "",
      parameters = ""
    )
  }
  val context = LocalContext.current
  TilePreviewData(
    onTileRequest = { request ->
      TilePreviewHelper.singleTimelineEntryTileBuilder(
        LinkTileRenderer(context).renderTile(LinkTileState(list), request.deviceConfiguration)
      ).build()
    }
  )
}
