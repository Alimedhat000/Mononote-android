package com.mononote.app.ui.theme

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Themed snackbar host: renders every snackbar as a Mononote card — the
 * card-surface fill and the same 24dp corner roundness as the editor card —
 * so toasts (archived/deleted, go-live messages) match the rest of the UI.
 */
@Composable
fun MononoteSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    SnackbarHost(hostState, modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.medium,
            containerColor = colors.cardSurface,
            contentColor = colors.primaryText,
            actionContentColor = colors.primaryText,
        )
    }
}

/**
 * Themed confirmation dialog: card-surface container with the app's 24dp
 * corners, so delete/restore confirmations match the cards around them.
 */
@Composable
fun MononoteAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
) {
    val colors = LocalMononoteColors.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = title,
        text = text,
        shape = MaterialTheme.shapes.medium,
        containerColor = colors.cardSurface,
        titleContentColor = colors.primaryText,
        textContentColor = colors.primaryText,
    )
}
