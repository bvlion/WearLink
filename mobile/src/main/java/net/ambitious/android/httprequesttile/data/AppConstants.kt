package net.ambitious.android.httprequesttile.data

object AppConstants {
  const val LIGHT_TERMS_OF_USE_URL = "https://wear-link.ambitious-i.net/light/terms_of_use.html"
  const val DARK_TERMS_OF_USE_URL = "https://wear-link.ambitious-i.net/dark/terms_of_use.html"
  const val LIGHT_PRIVACY_POLICY_URL = "https://wear-link.ambitious-i.net/light/privacy_policy.html"
  const val DARK_PRIVACY_POLICY_URL = "https://wear-link.ambitious-i.net/dark/privacy_policy.html"
  const val INQUIRY_URL = "https://forms.gle/LV4HMAfwb9JxwfRG8"

  enum class ViewMode(val type: Int) {
    DEFAULT(0),
    LIGHT(1),
    DARK(2)
  }

  fun isDarkMode(viewMode: ViewMode, isSystemInDarkTheme: Boolean) = when (viewMode) {
    ViewMode.DEFAULT -> isSystemInDarkTheme
    ViewMode.LIGHT -> false
    ViewMode.DARK -> true
  }
}
