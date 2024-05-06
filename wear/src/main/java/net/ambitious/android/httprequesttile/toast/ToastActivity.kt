package net.ambitious.android.wearnetvoyage.toast

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class ToastActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent.getStringExtra(EXTRA_TOAST_MESSAGE)?.let {
      Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
    intent = null
    finish()
  }

  companion object {
    internal const val EXTRA_TOAST_MESSAGE = "toastMessage"
  }
}