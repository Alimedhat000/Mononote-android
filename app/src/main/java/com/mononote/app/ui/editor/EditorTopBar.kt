package com.mononote.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mononote.app.R
import com.mononote.app.ui.theme.LocalMononoteColors

/** Centered app title with the overflow menu pinned right. */
@Composable
fun EditorTopBar(
    canArchiveOrDelete: Boolean,
    onOpenArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
        )
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            OverflowMenu(
                canArchiveOrDelete = canArchiveOrDelete,
                onOpenArchive = onOpenArchive,
                onDelete = onDelete,
            )
        }
    }
}

/**
 * The overflow menu. Delete note is hidden while the note is blank; View
 * archived notes is always available. The bottom action bar archives the
 * current note, so the menu only reaches the archived-notes list.
 */
@Composable
private fun OverflowMenu(
    canArchiveOrDelete: Boolean,
    onOpenArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalMononoteColors.current
    val moreOptions = stringResource(R.string.more_options)
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        FilledIconButton(
            onClick = { menuExpanded = true },
            modifier =
                Modifier
                    .size(40.dp)
                    .semantics { contentDescription = moreOptions },
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.menuButtonFill,
                    contentColor = colors.menuButtonIcon,
                ),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(
                        modifier =
                            Modifier
                                .size(4.dp)
                                .background(colors.menuButtonIcon, CircleShape),
                    )
                }
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            if (canArchiveOrDelete) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_note)) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.view_archived_notes)) },
                onClick = {
                    menuExpanded = false
                    onOpenArchive()
                },
            )
        }
    }
}
