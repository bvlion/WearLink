package info.bvlion.wearlink.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

internal val wearColors = Colors(
  primary = Color(0xFFFF5252),
  primaryVariant = Color(0xFFFF8A80),
  secondary = Color(0xFFF4081F),
  background = Color(0xFF121212),
  surface = Color(0xFF121212),
  error = Color(0xFFCF6679),
  onPrimary = Color(0xFFFFFFFF),
  onSecondary = Color(0xFFFFFFFF),
  onBackground = Color(0xFFFFFFFF),
  onSurface = Color(0xFFFFFFFF),
  onError = Color(0xFF000000)
)

private val darkBlue = Color(0xFF202124)
val tilesColors = androidx.wear.protolayout.material.Colors(
  wearColors.primary.toArgb(),
  wearColors.onPrimary.toArgb(),
  darkBlue.toArgb(),
  wearColors.onSurface.toArgb(),
)

@Composable
fun WearLinkTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colors = wearColors,
    content = content
  )
}
