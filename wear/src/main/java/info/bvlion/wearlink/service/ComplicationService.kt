package info.bvlion.wearlink.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import info.bvlion.wearlink.data.AppDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.httpexecute.HttpExecuteActivity
import info.bvlion.wearlink.service.ComplicationService.Companion.getPlainComplicationText
import info.bvlion.wearlink.toast.ToastActivity
import info.bvlion.wearlink.wear.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ComplicationService : ComplicationDataSourceService() {
  override fun getPreviewData(type: ComplicationType): ComplicationData =
    ShortTextComplicationData.Builder(
      text = getPlainComplicationText(this, R.string.complication_text),
      contentDescription = getPlainComplicationText(this, R.string.complication_description)
    )
      .setMonochromaticImage(createMonochromeImage(this))
      .build()

  override fun onComplicationRequest(
    request: ComplicationRequest,
    listener: ComplicationRequestListener
  ) {
    val context = this

    val savedRequest = runBlocking {
      AppDataStore.getDataStore(context).getSavedRequest.first()?.parseRequestParams()
    }

    if (savedRequest == null || savedRequest.none { it.watchfaceShortcut }) {
      listener.onComplicationData(
        ShortTextComplicationData.Builder(
          text = getPlainComplicationText(context, R.string.no_complication_text),
          contentDescription = getPlainComplicationText(context, R.string.no_complication_description)
        )
          .setMonochromaticImage(createMonochromeImage(this))
          .setTapAction(PendingIntent.getActivity(
            this,
            0,
            Intent(this, ToastActivity::class.java).apply {
              putExtra(ToastActivity.EXTRA_TOAST_MESSAGE, getString(R.string.no_complication_description))
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          ))
          .build()
      )
      return
    }

    val watchfaceRequest = savedRequest.first { it.watchfaceShortcut }

    listener.onComplicationData(
      ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(watchfaceRequest.title).build(),
        contentDescription = PlainComplicationText.Builder(getString(R.string.complication_main_description, watchfaceRequest.title)).build()
      )
        .setMonochromaticImage(createMonochromeImage(this))
        .setTapAction(PendingIntent.getActivity(
          this,
          0,
          Intent(this, HttpExecuteActivity::class.java).apply {
            putExtra(HttpExecuteActivity.EXTRA_REQUEST_TITLE, watchfaceRequest.title)
            putExtra(HttpExecuteActivity.EXTRA_REQUEST_PARAMS, watchfaceRequest.toJsonString())
            putExtra(HttpExecuteActivity.EXTRA_SHOW_CONFIRMATION, true)
          },
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ))
        .build()
    )
  }

  companion object {
    private fun createMonochromeImage(context: Context) = MonochromaticImage.Builder(
      Icon.createWithResource(context, R.drawable.ic_send)
    ).build()

    private fun getPlainComplicationText(context: Context, @StringRes textResId: Int) =
      PlainComplicationText.Builder(context.getString(textResId)).build()
  }
}