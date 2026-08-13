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

/**
 * Sets up the app's navigation graph: the [MononoteRoutes.EDITOR] screen as
 * the start destination and the [MononoteRoutes.ARCHIVE] archive-list
 * destination. The editor's overflow menu is wired to the archive route in
 * Phase 3.
 */
@Composable
fun MononoteNavHost(navController: NavHostController = rememberNavController()) {
    val app = LocalContext.current.applicationContext as MononoteApp
    NavHost(
        navController = navController,
        startDestination = MononoteRoutes.EDITOR,
    ) {
        composable(MononoteRoutes.EDITOR) {
            EditorScreen(repository = app.repository)
        }
        composable(MononoteRoutes.ARCHIVE) {
            ArchiveScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
