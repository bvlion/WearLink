package net.ambitious.android.wearnetvoyage.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import net.ambitious.android.wearnetvoyage.R
import net.ambitious.android.wearnetvoyage.analytics.AppAnalytics
import net.ambitious.android.wearnetvoyage.data.AppConstants
import net.ambitious.android.wearnetvoyage.data.Constant
import net.ambitious.android.wearnetvoyage.data.RequestParams
import net.ambitious.android.wearnetvoyage.theme.LinkTileTheme

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
        AppAnalytics.logEvent(
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
  .setPrimaryLabelTextContent(
    Text.Builder(context, context.getString(R.string.app_name))
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
      .setColor(argb(LinkTileTheme.colors.primary))
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
      "同期",
      ModifiersBuilders.Clickable.Builder()
        .setId(AppConstants.SYNC_STORE_DATA)
        .setOnClick(ActionBuilders.LoadAction.Builder().build())
        .build(),
      deviceParameters
    )
      .setChipColors(ChipColors.primaryChipColors(LinkTileTheme.colors))
      .build()
  )
  .build()

@OptIn(ExperimentalHorologistApi::class)
@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun MessagingTileRendererPreview() {
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
  TileLayoutPreview(
    state = LinkTileState(list),
    resourceState = true,
    renderer = LinkTileRenderer(context)
  )
}