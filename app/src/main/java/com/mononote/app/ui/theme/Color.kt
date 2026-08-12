package com.mononote.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Mononote's color palette, exposed through a custom CompositionLocal rather
 * than stock Material 3 defaults so screens share one set of tokens.
 *
 * @param background App background.
 * @param cardSurface The editor card surface.
 * @param primaryText Primary text.
 * @param secondaryText Secondary/placeholder text; #9A9A9E in both themes.
 * @param doneButtonFill Fill of the "Done" pill button.
 * @param doneButtonText Text color on the "Done" pill button.
 * @param menuButtonFill Fill of the circular overflow-menu button.
 * @param menuButtonIcon Icon color of the overflow-menu button.
 * @param statusRingIdle Color of the idle autosave status ring.
 */
@Immutable
data class MononoteColors(
    val background: Color,
    val cardSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val doneButtonFill: Color,
    val doneButtonText: Color,
    val menuButtonFill: Color,
    val menuButtonIcon: Color,
    val statusRingIdle: Color,
)

/** Light theme palette: white background, black text, gray surfaces. */
val LightMononoteColors =
    MononoteColors(
        background = Color(0xFFFFFFFF),
        cardSurface = Color(0xFFF0F0F2),
        primaryText = Color(0xFF000000),
        secondaryText = Color(0xFF9A9A9E),
        doneButtonFill = Color(0xFF000000),
        doneButtonText = Color(0xFFFFFFFF),
        menuButtonFill = Color(0xFFF0F0F2),
        menuButtonIcon = Color(0xFF000000),
        statusRingIdle = Color(0xFF9A9A9E),
    )

/** Dark theme palette: near-black background, white text, dark gray surfaces. */
val DarkMononoteColors =
    MononoteColors(
        background = Color(0xFF121212),
        cardSurface = Color(0xFF1C1C1E),
        primaryText = Color(0xFFFFFFFF),
        secondaryText = Color(0xFF9A9A9E),
        doneButtonFill = Color(0xFFFFFFFF),
        doneButtonText = Color(0xFF000000),
        menuButtonFill = Color(0xFF2C2C2E),
        menuButtonIcon = Color(0xFFFFFFFF),
        statusRingIdle = Color(0xFF9A9A9E),
    )

/**
 * CompositionLocal carrying the active palette; defaults to the light theme.
 * Provided by [MononoteTheme].
 */
val LocalMononoteColors = staticCompositionLocalOf { LightMononoteColors }
