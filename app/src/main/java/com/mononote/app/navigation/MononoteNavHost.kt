package com.mononote.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mononote.app.MononoteApp
import com.mononote.app.ui.archive.ArchiveScreen
import com.mononote.app.ui.editor.EditorScreen
import com.mononote.app.ui.settings.AboutScreen
import com.mononote.app.ui.settings.SettingsScreen

/**
 * Sets up the app's navigation graph: the [MononoteRoutes.EDITOR] screen as
 * the start destination, the [MononoteRoutes.ARCHIVE] archive-list
 * destination, and the settings screens ([MononoteRoutes.SETTINGS] with its
 * about subscreen). The editor's overflow menu and go-live action bar are
 * wired to the archive route and the live-note controller.
 */
@Composable
fun MononoteNavHost(navController: NavHostController = rememberNavController()) {
    val app = LocalContext.current.applicationContext as MononoteApp
    NavHost(
        navController = navController,
        startDestination = MononoteRoutes.EDITOR,
    ) {
        composable(MononoteRoutes.EDITOR) {
            EditorScreen(
                repository = app.repository,
                onOpenArchive = { navController.navigate(MononoteRoutes.ARCHIVE) },
                onOpenSettings = { navController.navigate(MononoteRoutes.SETTINGS) },
                liveNoteController = app.liveNoteController,
            )
        }
        composable(MononoteRoutes.ARCHIVE) {
            ArchiveScreen(
                repository = app.repository,
                onBack = { navController.popBackStack() },
            )
        }
        composable(MononoteRoutes.SETTINGS) {
            SettingsScreen(
                settingsDataStore = app.settingsDataStore,
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(MononoteRoutes.SETTINGS_ABOUT) },
            )
        }
        composable(MononoteRoutes.SETTINGS_ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
