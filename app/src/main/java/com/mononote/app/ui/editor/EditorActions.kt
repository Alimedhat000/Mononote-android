package com.mononote.app.ui.editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mononote.app.R
import com.mononote.app.notification.LiveNoteService
import com.mononote.app.ui.icons.LucideArchiveIcon
import com.mononote.app.ui.theme.LocalMononoteColors
import kotlinx.coroutines.launch

/** Full-width pill that only dismisses the keyboard; it never saves. */
@Composable
internal fun DoneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.large,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = colors.doneButtonFill,
                contentColor = colors.doneButtonText,
            ),
    ) {
        Text(
            text = stringResource(R.string.done),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * The go-live action bar shown instead of the Done pill while the note is not
 * being edited: view archived notes, go live, and delete, left to right. Go
 * live and delete are hidden while the note is blank.
 */
@Composable
internal fun NoteActionsBar(
    isLive: Boolean,
    canArchiveOrDelete: Boolean,
    onOpenArchive: () -> Unit,
    onDelete: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val colors = LocalMononoteColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionCircleButton(
            icon = LucideArchiveIcon,
            contentDescription = stringResource(R.string.archived_notes),
            containerColor = colors.menuButtonFill,
            contentColor = colors.menuButtonIcon,
            onClick = onOpenArchive,
        )
        if (canArchiveOrDelete) {
            GoLiveButton(isLive = isLive, snackbarHostState = snackbarHostState)
            ActionCircleButton(
                icon = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_note),
                containerColor = colors.menuButtonFill,
                contentColor = colors.menuButtonIcon,
                onClick = onDelete,
            )
        }
    }
}

/**
 * The go-live toggle: an outlined pill labelled "Go live" that fills and
 * relabels to "Live" while the live-note service is running. Requests the
 * notification permission when needed before starting; a denial surfaces a
 * snackbar.
 */
@Composable
internal fun GoLiveButton(
    isLive: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionNeeded = stringResource(R.string.notification_permission_needed)
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                context.startForegroundService(Intent(context, LiveNoteService::class.java))
            } else {
                scope.launch { snackbarHostState.showSnackbar(permissionNeeded) }
            }
        }
    Surface(
        modifier =
            modifier
                .height(48.dp)
                .clip(CircleShape)
                .clickable {
                    if (isLive) {
                        context.stopService(Intent(context, LiveNoteService::class.java))
                    } else if (
                        Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startForegroundService(Intent(context, LiveNoteService::class.java))
                    }
                },
        shape = CircleShape,
        color = if (isLive) colors.doneButtonFill else Color.Transparent,
        contentColor = if (isLive) colors.doneButtonText else colors.doneButtonFill,
        border = BorderStroke(1.dp, colors.doneButtonFill),
    ) {
        Text(
            text = stringResource(if (isLive) R.string.live else R.string.go_live),
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 13.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/** Circular icon button, the shared visual for the go-live action bar. */
@Composable
internal fun ActionCircleButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
