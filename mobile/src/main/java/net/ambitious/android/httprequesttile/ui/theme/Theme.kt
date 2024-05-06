package net.ambitious.android.wearnetvoyage.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

private val DarkColorPalette = darkColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = darkBackground,
    surface = darkSurface,
    error = darkError,
    onPrimary = darkOnPrimary,
    onSecondary = darkOnSecondary,
    onBackground = darkOnBackground,
    onSurface = darkOnSurface,
    onError = darkOnError
)

private val LightColorPalette = lightColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    background = lightBackground,
    surface = lightSurface,
    error = lightError,
    onPrimary = lightOnPrimary,
    onSecondary = lightOnSecondary,
    onBackground = lightOnBackground,
    onSurface = lightOnSurface,
    onError = lightOnError
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

inline fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    crossinline onClick: ()->Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}