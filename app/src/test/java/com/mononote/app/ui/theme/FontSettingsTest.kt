package com.mononote.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.ThemeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Pins the settings → theme wiring: a persisted font/theme option must map to
 * the Compose values that [MononoteTheme] actually consumes, so a change in
 * Settings shows up in the rendered typography and palette.
 */
class FontSettingsTest {
    @Test
    fun `theme mode resolves to the matching dark decision`() {
        assertFalse(ThemeMode.SYSTEM.resolveDarkTheme(systemInDarkMode = false))
        assertTrue(ThemeMode.SYSTEM.resolveDarkTheme(systemInDarkMode = true))
        assertFalse(ThemeMode.LIGHT.resolveDarkTheme(systemInDarkMode = true))
        assertTrue(ThemeMode.DARK.resolveDarkTheme(systemInDarkMode = false))
    }

    @Test
    fun `font family option maps to the matching compose family`() {
        assertEquals(FontFamily.Default, FontFamilyOption.DEFAULT.composeFamily)
        assertEquals(FontFamily.Serif, FontFamilyOption.SERIF.composeFamily)
        assertEquals(FontFamily.Monospace, FontFamilyOption.MONOSPACE.composeFamily)
    }

    @Test
    fun `font size option maps to the matching text size`() {
        assertEquals(16.sp, FontSizeOption.SMALL.bodyTextSize)
        assertEquals(18.sp, FontSizeOption.MEDIUM.bodyTextSize)
        assertEquals(22.sp, FontSizeOption.LARGE.bodyTextSize)
    }

    @Test
    fun `typography carries the selected family into body and title text`() {
        val typography = mononoteTypography(fontFamily = FontFamily.Serif)

        assertEquals(FontFamily.Serif, typography.bodyLarge.fontFamily)
        assertEquals(FontFamily.Serif, typography.titleLarge.fontFamily)
    }

    @Test
    fun `typography carries the selected size into the body text`() {
        val typography = mononoteTypography(fontFamily = FontFamily.Monospace, bodyTextSize = 22.sp)

        assertEquals(FontFamily.Monospace, typography.bodyLarge.fontFamily)
        assertEquals(22.sp, typography.bodyLarge.fontSize)
    }
}
