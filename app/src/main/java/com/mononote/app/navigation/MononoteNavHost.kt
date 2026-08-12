package com.mononote.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mononote.app.ui.archive.ArchiveScreen
import com.mononote.app.ui.editor.EditorScreen

object MononoteRoutes {
    const val EDITOR = "editor"
    const val ARCHIVE = "archive"
}

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
