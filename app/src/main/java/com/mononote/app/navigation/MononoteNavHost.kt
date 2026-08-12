package com.mononote.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mononote.app.ui.archive.ArchiveScreen
import com.mononote.app.ui.editor.EditorScreen

/**
 * Sets up the app's navigation graph: the [MononoteRoutes.EDITOR] screen as
 * the start destination, with [MononoteRoutes.ARCHIVE] reachable from it.
 */
@Composable
fun MononoteNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = MononoteRoutes.EDITOR,
    ) {
        composable(MononoteRoutes.EDITOR) {
            EditorScreen(
                onOpenArchive = { navController.navigate(MononoteRoutes.ARCHIVE) },
            )
        }
        composable(MononoteRoutes.ARCHIVE) {
            ArchiveScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
