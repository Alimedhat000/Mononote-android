package com.mononote.app.ui.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mononote.app.R
import com.mononote.app.data.NotesRepository
import com.mononote.app.ui.theme.LocalMononoteColors

/**
 * The editor screen: the app's single note, autosaved as you type.
 *
 * Layout, top to bottom: a top bar with the app title and an overflow-menu
 * button, a rounded card holding the note text field with the autosave status
 * indicator at its bottom-right corner, and a full-width Done pill that only
 * dismisses the keyboard. The overflow menu, archive/delete dialogs, snackbars,
 * and the archive navigation arrive in a later task.
 *
 * @param repository Single source of truth for note data; injects the
 *   [EditorViewModel].
 * @param modifier Modifier for the root column.
 */
@Composable
fun EditorScreen(
    repository: NotesRepository,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.factory(repository)),
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    viewModel.flushPendingText()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        EditorTopBar()
        Spacer(Modifier.height(12.dp))
        EditorCard(
            text = text,
            onTextChange = viewModel::updateText,
            isSaving = isSaving,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(16.dp))
        DoneButton()
    }
}

/** App title on the left and the circular overflow-menu button on the right. */
@Composable
private fun EditorTopBar(modifier: Modifier = Modifier) {
    val colors = LocalMononoteColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = colors.primaryText,
        )
        FilledIconButton(
            onClick = {},
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.menuButtonFill,
                    contentColor = colors.menuButtonIcon,
                ),
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
    }
}

/**
 * The note surface: a rounded card with a borderless multiline text field and
 * the autosave status indicator pinned to its bottom-right corner.
 */
@Composable
private fun EditorCard(
    text: String,
    onTextChange: (String) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.primaryText),
                placeholder = {
                    Text(
                        text = stringResource(R.string.placeholder_text),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = colors.secondaryText,
                        unfocusedPlaceholderColor = colors.secondaryText,
                        cursorColor = colors.primaryText,
                    ),
            )
            AutosaveStatusIndicator(
                isSaving = isSaving,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

/**
 * Two-state autosave status: a thin outlined ring at rest, an indeterminate
 * spinner while a debounced save is writing.
 */
@Composable
private fun AutosaveStatusIndicator(
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Box(
        modifier = modifier.size(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = colors.primaryText,
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .border(width = 2.dp, color = colors.statusRingIdle, shape = CircleShape),
            )
        }
    }
}

/** Full-width pill that only dismisses the keyboard; it never saves. */
@Composable
private fun DoneButton(modifier: Modifier = Modifier) {
    val colors = LocalMononoteColors.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Button(
        onClick = { keyboardController?.hide() },
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
        )
    }
}
