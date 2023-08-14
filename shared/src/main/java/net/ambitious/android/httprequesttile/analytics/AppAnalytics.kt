package net.ambitious.android.httprequesttile.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AppAnalytics {
  private val analytics by lazy { Firebase.analytics }

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