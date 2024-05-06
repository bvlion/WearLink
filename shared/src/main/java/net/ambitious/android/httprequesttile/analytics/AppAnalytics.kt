package net.ambitious.android.wearnetvoyage.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AppAnalytics {
  private val analytics by lazy { Firebase.analytics }

  const val EVENT_TAB_TAP = "tab_tap"
  const val PARAM_EVENT_TAB_TAP_INDEX = "tab_tap_index"
  const val EVENT_REQUEST_SAVE_TAP = "request_save_tap"
  const val PARAM_EVENT_REQUEST_SAVE_COUNT = "request_save_count"
  const val EVENT_START = "start"
  const val PARAM_EVENT_START_REQUEST_SAVED_COUNT = "start_request_saved_count"
  const val EVENT_RESPONSE_SAVE_TAP = "response_save_tap"
  const val PARAM_EVENT_RESPONSE_SAVE_COUNT = "response_save_count"

  const val EVENT_TILE_HEADER_TAP = "tile_header_tap"
  const val EVENT_TILE_REQUEST_TAP = "tile_request_tap"
  const val PARAM_EVENT_TILE_REQUEST_TITLE_HASH = "tile_request_title_hash"
  const val EVENT_TILE_SYNC_TAP = "tile_sync_tap"

  fun logEvent(event: String, params: Map<String, String> = emptyMap()) {
    analytics.logEvent(event, params.toBundle())
  }

  private fun Map<String, String>.toBundle() = Bundle().apply {
    entries.forEach { (key, value) -> putString(key, value) }
  }
}