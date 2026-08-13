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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mononote.app.R
import com.mononote.app.ui.theme.LocalMononoteColors
import com.composables.icons.lucide.R as LucideR

/** Centered app title with the overflow menu pinned right. */
@Composable
fun EditorTopBar(
    onOpenArchive: () -> Unit,
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
            OverflowMenu(onOpenArchive = onOpenArchive)
        }
    }
}

/**
 * The overflow menu: Archived Notes and Settings, always available. Settings is
 * a placeholder entry for a later phase (dark/light mode, font, widget and
 * live-notification settings, feedback, about). The bottom action bar already
 * archives and deletes the current note, so the menu does not repeat it.
 */
@Composable
private fun OverflowMenu(onOpenArchive: () -> Unit) {
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
            DropdownMenuItem(
                leadingIcon = {
                    OverflowMenuIcon(iconId = LucideR.drawable.lucide_ic_archive)
                },
                text = { Text(stringResource(R.string.archived_notes)) },
                onClick = {
                    menuExpanded = false
                    onOpenArchive()
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    OverflowMenuIcon(iconId = LucideR.drawable.lucide_ic_settings)
                },
                text = { Text(stringResource(R.string.settings)) },
                onClick = { menuExpanded = false },
            )
        }
    }
}

/** Leading icon for an overflow-menu item, tinted to the muted secondary gray. */
@Composable
private fun OverflowMenuIcon(iconId: Int) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = null,
        tint = LocalMononoteColors.current.secondaryText,
    )
}
