package com.mononote.app.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mononote.app.R
import com.mononote.app.data.Note
import com.mononote.app.data.NotesRepository
import com.mononote.app.ui.theme.LocalMononoteColors
import com.mononote.app.ui.theme.MononoteTheme
import kotlinx.coroutines.flow.SharedFlow
import java.text.DateFormat
import java.util.Date
import com.composables.icons.lucide.R as LucideR

/**
 * The archive screen: every archived note as a card with its text, a relative
 * timestamp, and per-row restore / permanent-delete actions.
 *
 * Restore follows the app's product rule: with an empty or missing active note
 * the restored note becomes active and the screen returns to the editor; with
 * a non-blank active note a confirmation dialog is shown first, because the
 * current note will be archived to make room. Delete is permanent, so it
 * always asks for confirmation. A delete surfaces a snackbar; a successful
 * restore navigates back so the editor shows the restored note.
 *
 * @param repository Single source of truth for note data; injects the
 *   [ArchiveViewModel].
 * @param onBack Returns to the editor.
 * @param modifier Modifier for the root column.
 */
@Composable
fun ArchiveScreen(
    repository: NotesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = viewModel(factory = ArchiveViewModel.factory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    ArchiveEvents(events = viewModel.events, onBack = onBack, snackbarHostState = snackbarHostState)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp),
    ) {
        ArchiveTopBar(onBack = onBack, modifier = Modifier.padding(vertical = 8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (uiState.notes.isEmpty()) {
                EmptyArchiveState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        ArchivedNoteRow(
                            note = note,
                            onRestore = { viewModel.onRestoreClick(note) },
                            onDelete = { viewModel.onDeleteClick(note) },
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState)
    }

    uiState.pendingDelete?.let {
        DeleteNoteDialog(
            onDismiss = viewModel::onDeleteDismiss,
            onConfirm = viewModel::onDeleteConfirm,
        )
    }
    uiState.pendingRestore?.let {
        RestoreNoteDialog(
            onDismiss = viewModel::onRestoreDismiss,
            onConfirm = viewModel::onRestoreConfirm,
        )
    }
}

/** Back arrow and title, matching the editor's top-bar typography. */
@Composable
private fun ArchiveTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Text(
            text = stringResource(R.string.archived_notes),
            style = MaterialTheme.typography.titleLarge,
            color = colors.primaryText,
        )
    }
}

/** Centered empty-state message when nothing has been archived yet. */
@Composable
private fun EmptyArchiveState(modifier: Modifier = Modifier) {
    val colors = LocalMononoteColors.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.no_archived_notes),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * One archived note: truncated text, a relative timestamp, and circular
 * restore / delete actions.
 */
@Composable
private fun ArchivedNoteRow(
    note: Note,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = archivedTimeLabel(note),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                )
            }
            Spacer(Modifier.width(8.dp))
            ArchiveRowActionButton(
                icon = painterResource(LucideR.drawable.lucide_ic_archive_restore),
                contentDescription = stringResource(R.string.restore),
                onClick = onRestore,
            )
            ArchiveRowActionButton(
                icon = painterResource(LucideR.drawable.lucide_ic_trash),
                contentDescription = stringResource(R.string.delete_note),
                onClick = onDelete,
            )
        }
    }
}

/**
 * The archived note's age as a short label ("Just now", "5m ago", ...). Notes
 * archived a week or more ago fall back to a localized date.
 */
@Composable
private fun archivedTimeLabel(note: Note): String {
    val label = relativeTimeLabel(note.archivedAt ?: note.updatedAt, System.currentTimeMillis())
    return when (label) {
        RelativeTimeLabel.JustNow -> stringResource(R.string.just_now)
        is RelativeTimeLabel.MinutesAgo -> stringResource(R.string.minutes_ago, label.minutes)
        is RelativeTimeLabel.HoursAgo -> stringResource(R.string.hours_ago, label.hours)
        is RelativeTimeLabel.DaysAgo -> stringResource(R.string.days_ago, label.days)
        is RelativeTimeLabel.OnDate -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(label.epochMillis))
    }
}

/** Circular icon button for the per-row restore / delete actions. */
@Composable
private fun ArchiveRowActionButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.menuButtonFill,
                contentColor = colors.menuButtonIcon,
            ),
    ) {
        Icon(painter = icon, contentDescription = contentDescription)
    }
}

/** Surfaces delete snackbars and restore-triggered navigation. */
@Composable
private fun ArchiveEvents(
    events: SharedFlow<ArchiveEvent>,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val deleted = stringResource(R.string.note_deleted)
    LaunchedEffect(events, onBack) {
        events.collect { event ->
            when (event) {
                ArchiveEvent.NOTE_DELETED -> snackbarHostState.showSnackbar(deleted)
                ArchiveEvent.NAVIGATE_BACK -> onBack()
            }
        }
    }
}

/** Confirmation dialog before the permanent delete. */
@Composable
private fun DeleteNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_note_dialog_title)) },
        text = { Text(stringResource(R.string.delete_note_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/** Confirmation dialog before restoring over a non-blank active note. */
@Composable
private fun RestoreNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.restore_note_dialog_title)) },
        text = { Text(stringResource(R.string.restore_note_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/** Used by the archive screen preview to render sample rows. */
@Preview(showBackground = true, name = "Archived note row")
@Composable
private fun ArchiveScreenPreview() {
    MononoteTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ArchivedNoteRow(
                note =
                    Note(
                        id = 1,
                        text = "Buy oat milk and a good coffee bean",
                        createdAt = 1000,
                        updatedAt = 1000,
                        archivedAt = System.currentTimeMillis() - 5 * 60 * 1000,
                    ),
                onRestore = {},
                onDelete = {},
            )
        }
    }
}
