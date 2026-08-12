package com.mononote.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

val LocalMononoteColors = staticCompositionLocalOf { LightMononoteColors }
