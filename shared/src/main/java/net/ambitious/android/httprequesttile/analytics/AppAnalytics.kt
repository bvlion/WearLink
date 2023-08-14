package net.ambitious.android.httprequesttile.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AppAnalytics {
  private val analytics by lazy { Firebase.analytics }

  const val EVENT_TILE_HEADER_CLICK = "tile_header_click"
  const val EVENT_TILE_REQUEST_CLICK = "tile_request_click"
  const val PARAM_EVENT_TILE_REQUEST_TITLE_HASH = "tile_request_title_hash"
  const val EVENT_TILE_SYNC_CLICK = "tile_sync_click"

  fun logEvent(event: String, params: Map<String, String> = emptyMap()) {
    analytics.logEvent(event, params.toBundle())
  }

  private fun Map<String, String>.toBundle() = Bundle().apply {
    entries.forEach { (key, value) -> putString(key, value) }
  }
}