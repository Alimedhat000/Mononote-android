package com.mononote.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Mononote typography: 18sp semibold titles and regular body text sized by
 * [bodyTextSize] (the note text), both in [fontFamily]. The family and body
 * size come from the settings screen; the platform default is 18sp Roboto.
 */
fun mononoteTypography(
    fontFamily: FontFamily = FontFamily.Default,
    bodyTextSize: TextUnit = 18.sp,
): Typography =
    Typography(
        titleLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = bodyTextSize,
            ),
    )
