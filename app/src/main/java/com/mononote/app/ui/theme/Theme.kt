package com.mononote.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme =
    lightColorScheme(
        background = LightMononoteColors.background,
        surface = LightMononoteColors.cardSurface,
        surfaceVariant = LightMononoteColors.cardSurface,
        onBackground = LightMononoteColors.primaryText,
        onSurface = LightMononoteColors.primaryText,
        onSurfaceVariant = LightMononoteColors.secondaryText,
        primary = LightMononoteColors.primaryText,
        onPrimary = LightMononoteColors.background,
    )

private val DarkColorScheme =
    darkColorScheme(
        background = DarkMononoteColors.background,
        surface = DarkMononoteColors.cardSurface,
        surfaceVariant = DarkMononoteColors.cardSurface,
        onBackground = DarkMononoteColors.primaryText,
        onSurface = DarkMononoteColors.primaryText,
        onSurfaceVariant = DarkMononoteColors.secondaryText,
        primary = DarkMononoteColors.primaryText,
        onPrimary = DarkMononoteColors.background,
    )

/** Mononote shapes: 24dp rounded cards ([MononoteColors.cardSurface]) and pill buttons. */
val MononoteShapes =
    Shapes(
        medium = RoundedCornerShape(24.dp),
        large = CircleShape,
    )

/**
 * Applies the Mononote theme: a light/dark palette exposed via
 * [LocalMononoteColors], plus the Mononote typography and shapes.
 *
 * @param darkTheme Whether the dark palette is active. Defaults to following
 *   the system; the settings screen can force a scheme.
 * @param fontFamily Family for the note text and app titles.
 * @param bodyTextSize Size of the note text (bodyLarge).
 * @param content The rest of the app UI.
 */
@Composable
fun MononoteTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    fontFamily: FontFamily = FontFamily.Default,
    bodyTextSize: TextUnit = 18.sp,
    content: @Composable () -> Unit,
) {
    val mononoteColors = if (darkTheme) DarkMononoteColors else LightMononoteColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalMononoteColors provides mononoteColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = mononoteTypography(fontFamily, bodyTextSize),
            shapes = MononoteShapes,
            content = content,
        )
    }
}
