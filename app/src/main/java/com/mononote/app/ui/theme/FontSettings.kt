package com.mononote.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.ThemeMode

/**
 * Maps a persisted [ThemeMode] to a concrete dark/light decision, given
 * whether the device is currently in dark mode.
 */
fun ThemeMode.resolveDarkTheme(systemInDarkMode: Boolean): Boolean =
    when (this) {
        ThemeMode.SYSTEM -> systemInDarkMode
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

/** The Compose [FontFamily] a persisted [FontFamilyOption] stands for. */
val FontFamilyOption.composeFamily: FontFamily
    get() =
        when (this) {
            FontFamilyOption.DEFAULT -> FontFamily.Default
            FontFamilyOption.SERIF -> FontFamily.Serif
            FontFamilyOption.MONOSPACE -> FontFamily.Monospace
        }

/** The note-text size in sp that a persisted [FontSizeOption] stands for. */
val FontSizeOption.bodyTextSize: TextUnit
    get() =
        when (this) {
            FontSizeOption.SMALL -> 16.sp
            FontSizeOption.MEDIUM -> 18.sp
            FontSizeOption.LARGE -> 22.sp
        }
