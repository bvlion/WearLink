package info.bvlion.wearlink.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable

@Composable
fun MainAnimatedVisibility(
  visible: Boolean,
  content: @Composable () -> Unit
) {
  AnimatedVisibility(
    visible = visible,
    enter = scaleIn(animationSpec = tween(durationMillis = 130)),
    exit = fadeOut(animationSpec = tween(durationMillis = 150))
  ) {
    content()
  }
}