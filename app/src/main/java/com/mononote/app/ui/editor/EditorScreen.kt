package com.mononote.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
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
import com.mononote.app.notification.LiveNoteController
import com.mononote.app.ui.theme.LocalMononoteColors
import kotlinx.coroutines.flow.SharedFlow

/**
 * The editor screen: the app's single note, autosaved as you type.
 *
 * A top bar with the app title and an overflow menu sits above a rounded card
 * with the note text field and a character-limit ring. The bottom area shows
 * the full-width Done pill while the field is focused, and otherwise the
 * go-live action bar: archive the current note, go live (a persistent live
 * notification of the note), and delete. The overflow menu reaches the
 * archived-notes screen and a settings entry (the settings screen lands in a
 * later phase). Archive and delete surface a snackbar and delete asks for
 * confirmation first.
 *
 * @param repository Single source of truth for note data; injects the
 *   [EditorViewModel].
 * @param onOpenArchive Navigates to the archived-notes screen.
 * @param liveNoteController Mirrors whether the live-note service is running.
 * @param modifier Modifier for the root column.
 */
@Composable
fun EditorScreen(
    repository: NotesRepository,
    onOpenArchive: () -> Unit,
    liveNoteController: LiveNoteController,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.factory(repository)),
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val characterLimitProgress by viewModel.characterLimitProgress.collectAsStateWithLifecycle()
    val canArchiveOrDelete by viewModel.canArchiveOrDelete.collectAsStateWithLifecycle()
    val isLive by liveNoteController.isLive.collectAsStateWithLifecycle()

    FlushOnPauseOrStop(onFlush = viewModel::flushPendingText)
    val snackbarHostState = remember { SnackbarHostState() }
    NoteEventSnackbar(events = viewModel.events, snackbarHostState = snackbarHostState)

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    DismissKeyboardWhenLeaving(isEditing = isEditing)

    Column(
        modifier =
            modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
    ) {
        EditorTopBar(
            onOpenArchive = onOpenArchive,
        )
        Spacer(Modifier.height(12.dp))
        CenteredEditorCard(
            text = text,
            onTextChange = viewModel::updateText,
            characterLimitProgress = characterLimitProgress,
            onFocusChanged = { isEditing = it },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(16.dp))
        AnimatedEditorActions(
            state =
                EditorBottomBarState(
                    isEditing = isEditing,
                    isLive = isLive,
                    canArchiveOrDelete = canArchiveOrDelete,
                    noteText = text,
                ),
            onArchive = viewModel::archiveActiveNote,
            onDelete = { showDeleteDialog = true },
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
                viewModel.flushPendingText()
            },
            snackbarHostState = snackbarHostState,
        )
        SnackbarHost(snackbarHostState)
    }

    DeleteNoteDialog(
        visible = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = viewModel::deleteActiveNote,
    )
}

/** Flushes in-progress text on onPause/onStop so process death can't lose it. */
@Composable
private fun FlushOnPauseOrStop(onFlush: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, onFlush) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    onFlush()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

/**
 * Ends the editing session: hides the keyboard and clears the field's focus.
 * System back does it while editing, and closing the keyboard by any other
 * means (its own hide button, swipe-down) unfocuses too, so the Done pill
 * gives way to the go-live action bar. Safe against hardware keyboards that
 * never raise the IME.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DismissKeyboardWhenLeaving(isEditing: Boolean) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboard: () -> Unit = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }
    BackHandler(enabled = isEditing) { dismissKeyboard() }
    val imeVisible = WindowInsets.isImeVisible
    var wasImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (wasImeVisible && !imeVisible && isEditing) {
            dismissKeyboard()
        }
        wasImeVisible = imeVisible
    }
}

/** Surfaces archive/delete one-shot events as snackbars. */
@Composable
private fun NoteEventSnackbar(
    events: SharedFlow<EditorUiEvent>,
    snackbarHostState: SnackbarHostState,
) {
    val archived = stringResource(R.string.note_archived)
    val deleted = stringResource(R.string.note_deleted)
    LaunchedEffect(events) {
        events.collect { event ->
            val message =
                when (event) {
                    EditorUiEvent.NOTE_ARCHIVED -> archived
                    EditorUiEvent.NOTE_DELETED -> deleted
                }
            snackbarHostState.showSnackbar(message)
        }
    }
}

/** Confirmation dialog before the permanent delete. */
@Composable
private fun DeleteNoteDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_note_dialog_title)) },
        text = { Text(stringResource(R.string.delete_note_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                },
            ) {
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

/**
 * The note card, compact and vertically centered in the remaining space. Its
 * height snaps to [noteCardHeight] when the keyboard is hidden and to the
 * available space above the keyboard while it is shown.
 */
@Composable
private fun CenteredEditorCard(
    text: String,
    onTextChange: (String) -> Unit,
    characterLimitProgress: Float,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        EditorCard(
            text = text,
            onTextChange = onTextChange,
            characterLimitProgress = characterLimitProgress,
            onFocusChanged = onFocusChanged,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(150))
                    .height(noteCardHeight),
        )
    }
}

/**
 * The note surface: a rounded card with a borderless multiline text field and
 * the character-limit progress ring pinned to its bottom-right corner.
 * [onFocusChanged] reports the field's focus so the screen can swap the Done
 * pill for the go-live action bar.
 */
@Composable
private fun EditorCard(
    text: String,
    onTextChange: (String) -> Unit,
    characterLimitProgress: Float,
    onFocusChanged: (Boolean) -> Unit,
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onFocusChanged { onFocusChanged(it.isFocused) },
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
            CharacterLimitIndicator(
                progress = characterLimitProgress,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

/**
 * A ring that shows how close [progress] (`[0f, 1f]`) is to the character
 * limit: a static gray ring with a white arc on top that grows clockwise from
 * the top as the count rises. Drawn synchronously from [progress] — no
 * animation, so the arc never lags or wobbles behind the keystrokes. Purely
 * informational, it never blocks typing.
 */
@Composable
private fun CharacterLimitIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft =
            Offset(
                x = center.x - diameter / 2,
                y = center.y - diameter / 2,
            )
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = colors.statusRingIdle,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )
        if (progress > 0f) {
            drawArc(
                color = colors.primaryText,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * The note card's compact height when the keyboard is closed, matching the
 * size it naturally takes once the keyboard is open. The card clamps to the
 * available space when it is tighter than this (e.g. small screens).
 */
private val noteCardHeight = 320.dp
