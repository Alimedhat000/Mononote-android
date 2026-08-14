package com.mononote.app.navigation

/**
 * Navigation route names used by [MononoteNavHost].
 */
object MononoteRoutes {
    /** The single-note editor; the app's start destination. */
    const val EDITOR = "editor"

    /** The archived-notes list screen. */
    const val ARCHIVE = "archive"

    /** The settings screen. */
    const val SETTINGS = "settings"

    /** The about screen, reached from settings. */
    const val SETTINGS_ABOUT = "settings/about"
}
