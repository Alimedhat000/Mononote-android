package com.mononote.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
 * button, a rounded card holding the note text field with a character-limit
 * progress ring at its bottom-right corner, and a full-width Done pill that
 * only dismisses the keyboard. The overflow menu, archive/delete dialogs,
 * snackbars, and the archive navigation arrive in a later task.
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
    val characterLimitProgress by viewModel.characterLimitProgress.collectAsStateWithLifecycle()

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
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        EditorTopBar()
        Spacer(Modifier.height(12.dp))
        EditorCard(
            text = text,
            onTextChange = viewModel::updateText,
            characterLimitProgress = characterLimitProgress,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(16.dp))
        DoneButton()
    }
}

/** Centered app title on top with the overflow-menu button pinned right. */
@Composable
private fun EditorTopBar(modifier: Modifier = Modifier) {
    val colors = LocalMononoteColors.current
    val moreOptions = stringResource(R.string.more_options)
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
        )
        FilledIconButton(
            onClick = {},
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
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
    }
}

/**
 * The note surface: a rounded card with a borderless multiline text field and
 * the character-limit progress ring pinned to its bottom-right corner.
 */
@Composable
private fun EditorCard(
    text: String,
    onTextChange: (String) -> Unit,
    characterLimitProgress: Float,
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
            fontWeight = FontWeight.Bold,
        )
    }
}
