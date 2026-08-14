package com.mononote.app.data

/**
 * How the app follows the device theme.
 *
 * [SYSTEM] follows the OS setting, [LIGHT] and [DARK] force a scheme
 * regardless of it.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** The note text typeface, picked from platform families (no bundled fonts). */
enum class FontFamilyOption {
    DEFAULT,
    SERIF,
    MONOSPACE,
}

/** The note text size scale. */
enum class FontSizeOption {
    SMALL,
    MEDIUM,
    LARGE,
}
