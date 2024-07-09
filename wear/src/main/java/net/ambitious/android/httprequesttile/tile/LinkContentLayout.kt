package net.ambitious.android.httprequesttile.tile

import android.content.Context
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.layouts.MultiSlotLayout
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.theme.LinkTileTheme

object LinkContentLayout {
  fun create(
    context: Context,
    state: LinkTileState,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    requestClickableFactory: (RequestParams) -> ModifiersBuilders.Clickable
  ): LayoutElementBuilders.LayoutElement =
    MultiSlotLayout.Builder()
      .apply {
        when (state.requests.size) {
          1 -> addSlotContent(
            linkLayout1(
              context,
              state.requests[0],
              requestClickableFactory(state.requests[0]),
              deviceParameters
            )
          )
          2 -> addSlotContent(
            linkLayout2(
              context,
              state.requests[0],
              requestClickableFactory(state.requests[0]),
              state.requests[1],
              requestClickableFactory(state.requests[1]),
              deviceParameters
            )
          )
          3 -> {
            addSlotContent(
              linkLayout3has2(
                context,
                state.requests[0],
                requestClickableFactory(state.requests[0]),
                state.requests[1],
                requestClickableFactory(state.requests[1]),
                deviceParameters
              )
            )
            addSlotContent(
              linkLayout3has1(
                context,
                state.requests[2],
                requestClickableFactory(state.requests[2]),
                deviceParameters
              )
            )
          }
          4 -> {
            addSlotContent(
              linkLayout3has2(
                context,
                state.requests[0],
                requestClickableFactory(state.requests[0]),
                state.requests[1],
                requestClickableFactory(state.requests[1]),
                deviceParameters
              )
            )
            addSlotContent(
              linkLayout3has2(
                context,
                state.requests[2],
                requestClickableFactory(state.requests[2]),
                state.requests[3],
                requestClickableFactory(state.requests[3]),
                deviceParameters
              )
            )
          }
        }
      }.build()

  private fun linkLayout1(
    context: Context,
    requestParams: RequestParams,
    clickable: ModifiersBuilders.Clickable,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
  ) = LayoutElementBuilders.Column.Builder()
    .addContent(
      CompactChip.Builder(context, requestParams.title, clickable, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .build()

  private fun linkLayout2(
    context: Context,
    requestParams1: RequestParams,
    clickable1: ModifiersBuilders.Clickable,
    requestParams2: RequestParams,
    clickable2: ModifiersBuilders.Clickable,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
  ) = LayoutElementBuilders.Column.Builder()
    .addContent(
      CompactChip.Builder(context, requestParams1.title, clickable1, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .addContent(
      CompactChip.Builder(context, requestParams2.title, clickable2, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .build()

  private fun linkLayout3has1(
    context: Context,
    requestParams: RequestParams,
    clickable: ModifiersBuilders.Clickable,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
  ) = LayoutElementBuilders.Column.Builder()
    .addContent(
      CompactChip.Builder(context, createTitle(requestParams.title), clickable, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .build()

  private fun linkLayout3has2(
    context: Context,
    requestParams1: RequestParams,
    clickable1: ModifiersBuilders.Clickable,
    requestParams2: RequestParams,
    clickable2: ModifiersBuilders.Clickable,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
  ) = LayoutElementBuilders.Column.Builder()
    .addContent(
      CompactChip.Builder(context, createTitle(requestParams1.title), clickable1, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .addContent(
      CompactChip.Builder(context, createTitle(requestParams2.title), clickable2, deviceParameters)
        .setChipColors(ChipColors.secondaryChipColors(LinkTileTheme.colors))
        .build()
    )
    .build()

  private fun createTitle(text: String, maxCount: Int = 8): String {
    val sb = StringBuilder()
    var count = 0
    text.forEach {
      if (it.isFullWidth()) {
        count += 2
      } else {
        count++
      }
      if (count > maxCount) {
        return "$sb…"
      }
      sb.append(it)
    }
    return sb.toString()
  }

  private fun Char.isFullWidth(): Boolean {
    return when (this) {
      in '\u0020'..'\u007E' -> false // 半角
      in '\uFF61'..'\uFF9F' -> false // 半角カタカナ
      else -> true
    }
  }
}