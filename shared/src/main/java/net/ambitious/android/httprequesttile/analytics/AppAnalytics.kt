package net.ambitious.android.httprequesttile.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AppAnalytics {
  private val analytics by lazy { Firebase.analytics }

  fun logEvent(event: String, params: Map<String, String> = emptyMap()) {
    analytics.logEvent(event, params.toBundle())
  }

  private fun Map<String, String>.toBundle() = Bundle().apply {
    entries.forEach { (key, value) -> putString(key, value) }
  }
}