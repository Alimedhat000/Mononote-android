package com.mononote.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    background = LightMononoteColors.background,
    surface = LightMononoteColors.cardSurface,
    surfaceVariant = LightMononoteColors.cardSurface,
    onBackground = LightMononoteColors.primaryText,
    onSurface = LightMononoteColors.primaryText,
    onSurfaceVariant = LightMononoteColors.secondaryText,
    primary = LightMononoteColors.primaryText,
    onPrimary = LightMononoteColors.background,
)

private val DarkColorScheme = darkColorScheme(
    background = DarkMononoteColors.background,
    surface = DarkMononoteColors.cardSurface,
    surfaceVariant = DarkMononoteColors.cardSurface,
    onBackground = DarkMononoteColors.primaryText,
    onSurface = DarkMononoteColors.primaryText,
    onSurfaceVariant = DarkMononoteColors.secondaryText,
    primary = DarkMononoteColors.primaryText,
    onPrimary = DarkMononoteColors.background,
)

val MononoteShapes = Shapes(
    medium = RoundedCornerShape(24.dp),
    large = CircleShape,
)

@Composable
fun MononoteTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val mononoteColors = if (darkTheme) DarkMononoteColors else LightMononoteColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalMononoteColors provides mononoteColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MononoteTypography,
            shapes = MononoteShapes,
            content = content,
        )
    }
}
