package info.bvlion.wearlink.data

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.SystemBarStyle

object AppConstants {
  const val INQUIRY_URL = "https://forms.gle/LV4HMAfwb9JxwfRG8"

  enum class ViewMode(val type: Int) {
    DEFAULT(0),
    LIGHT(1),
    DARK(2)
  }

  fun isSystemInDarkTheme(context: Context): Boolean =
    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

  fun isDarkMode(viewMode: ViewMode, isSystemInDarkTheme: Boolean) = when (viewMode) {
    ViewMode.DEFAULT -> isSystemInDarkTheme
    ViewMode.LIGHT -> false
    ViewMode.DARK -> true
  }

  fun getStatusBarStyle(viewMode: ViewMode, isSystemInDarkTheme: Boolean) = when (viewMode) {
    ViewMode.DEFAULT -> if (isSystemInDarkTheme) {
      SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
      SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    ViewMode.LIGHT -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    ViewMode.DARK -> SystemBarStyle.dark(Color.TRANSPARENT)
  }

  fun getSystemBarStyle(viewMode: ViewMode) = when (viewMode) {
    ViewMode.DEFAULT -> SystemBarStyle.auto(DefaultLightScrim, DefaultDarkScrim)
    ViewMode.LIGHT -> SystemBarStyle.auto(DefaultLightScrim, DefaultDarkScrim)
    ViewMode.DARK -> SystemBarStyle.dark(DefaultDarkScrim)
  }

  private val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
  private val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
}