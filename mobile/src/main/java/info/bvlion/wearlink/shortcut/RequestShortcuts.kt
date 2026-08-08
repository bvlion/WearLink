package info.bvlion.wearlink.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import info.bvlion.wearlink.data.RequestParams

object RequestShortcuts {
  const val ACTION_EXECUTE_SHORTCUT = "info.bvlion.wearlink.action.EXECUTE_SHORTCUT"
  const val EXTRA_REQUEST_ID = "requestId"

  fun isSupported(context: Context): Boolean =
    ShortcutManagerCompat.isRequestPinShortcutSupported(context)

  fun requestPin(context: Context, request: RequestParams) {
    ShortcutManagerCompat.requestPinShortcut(context, buildShortcut(context, request), null)
  }

  fun updateLabel(context: Context, request: RequestParams) {
    ShortcutManagerCompat.updateShortcuts(context, listOf(buildShortcut(context, request)))
  }

  fun disable(context: Context, requestId: String, disabledMessage: String) {
    ShortcutManagerCompat.disableShortcuts(context, listOf(shortcutId(requestId)), disabledMessage)
  }

  internal fun shortcutId(requestId: String) = "request-$requestId"

  private fun buildShortcut(context: Context, request: RequestParams): ShortcutInfoCompat =
    ShortcutInfoCompat.Builder(context, shortcutId(request.id))
      .setShortLabel(request.title)
      .setIcon(IconCompat.createWithResource(context, info.bvlion.wearlink.shared.R.mipmap.ic_launcher))
      .setIntent(
        Intent(context, ShortcutExecuteActivity::class.java).apply {
          action = ACTION_EXECUTE_SHORTCUT
          putExtra(EXTRA_REQUEST_ID, request.id)
        }
      )
      .build()
}
