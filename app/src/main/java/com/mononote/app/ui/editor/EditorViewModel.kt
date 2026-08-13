package com.mononote.app.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mononote.app.data.Note
import com.mononote.app.data.NotesRepository
import com.mononote.app.data.isBlankNote
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/** One-shot events the editor surfaces as snackbars. */
enum class EditorUiEvent {
    /** The active note was archived (reversible). */
    NOTE_ARCHIVED,

    /** The active note was permanently deleted. */
    NOTE_DELETED,
}

/**
 * Drives the editor screen: holds the in-progress text, autosaves it after a
 * debounce, and coordinates archive/delete with the repository.
 *
 * The typed text lives in [text] and is the only thing the text field binds
 * to, so keystrokes never fight the repository flow. [observeActiveNote]
 * emissions only replace [text] when the active note disappears (archive or
 * delete) or the underlying row changes identity and its text differs from
 * what is known to be persisted — never when a debounced autosave echoes back
 * the same row, and never for the transient blank row the create path inserts
 * before the first write lands. Those echoes must not clobber text typed since
 * the save was scheduled.
 *
 * Autosaves flow through [repository.saveActiveNote], which writes Room and
 * the widget snapshot in one suspend call. A failed save is logged and the
 * next edit retries it; a failure never kills the autosave pipeline.
 * [flushPendingText] bypasses the debounce for process-death safety and is
 * called by the editor from onPause/onStop.
 *
 * [canArchiveOrDelete] reuses [Note.isBlankNote], the app-wide blank
 * definition, so the overflow menu's visibility can never diverge from the
 * restore flow. When no active note row exists yet, archive/delete are hidden
 * regardless of typed text: there is nothing to archive or delete.
 *
 * [characterLimitProgress] is purely visual — it tracks how close [text] is
 * to [MAX_NOTE_LENGTH] for the corner ring on the editor card. It does not
 * gate typing, saving, or archive/delete; those stay keyed off blankness only.
 *
 * In-progress text survives process death through [SavedStateHandle]: every
 * text change is mirrored to [DRAFT_KEY], and a restored draft takes
 * precedence over the (older) persisted note text when the note row reloads,
 * so the last keystrokes can never be clobbered by the previous save.
 *
 * @param repository Single source of truth for note data.
 * @param savedStateHandle Retains the in-progress draft across process death.
 */
@OptIn(FlowPreview::class)
class EditorViewModel(
    private val repository: NotesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _text = MutableStateFlow(savedStateHandle[DRAFT_KEY] ?: "")
    private val currentNote = MutableStateFlow<Note?>(null)
    private val _isSaving = MutableStateFlow(false)
    private val _canArchiveOrDelete = MutableStateFlow(false)
    private val _events = MutableSharedFlow<EditorUiEvent>(extraBufferCapacity = 1)

    /** True until the note flow first emits after a process-death restore. */
    private var restoredDraft = _text.value.isNotBlank()

    /** Current editor text, updated immediately on each keystroke. */
    val text: StateFlow<String> = _text.asStateFlow()

    /** True while a debounced autosave is writing to the repository. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    /** Whether Archive/Delete may run: an active note exists and is not blank. */
    val canArchiveOrDelete: StateFlow<Boolean> = _canArchiveOrDelete.asStateFlow()

    /**
     * How full the character-limit ring should be, in `[0f, 1f]`.
     * `text.length / MAX_NOTE_LENGTH`, clamped — never exceeds 1f even past the limit.
     */
    val characterLimitProgress: StateFlow<Float> =
        _text
            .map { (it.length.toFloat() / MAX_NOTE_LENGTH).coerceIn(0f, 1f) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    /** One-shot snackbar events (archive/delete). */
    val events: SharedFlow<EditorUiEvent> = _events.asSharedFlow()

    /** The most recent text known to be persisted in the repository. */
    private var lastSaved: String = ""

    init {
        viewModelScope.launch {
            repository.observeActiveNote().collect(::applyNote)
        }
        viewModelScope.launch {
            _text
                .debounce(AUTOSAVE_DEBOUNCE_MILLIS)
                .collect(::autosave)
        }
    }

    /** Records the latest keystroke and mirrors it to the saved state. */
    fun updateText(newText: String) {
        setText(newText)
    }

    /** Archives the active note (reversible) and surfaces a snackbar. */
    fun archiveActiveNote() {
        viewModelScope.launch {
            if (!_canArchiveOrDelete.value) return@launch
            repository.archiveActiveNote()
            clearActiveNote()
            _events.tryEmit(EditorUiEvent.NOTE_ARCHIVED)
        }
    }

    /** Permanently deletes the active note and surfaces a snackbar. */
    fun deleteActiveNote() {
        viewModelScope.launch {
            if (!_canArchiveOrDelete.value) return@launch
            repository.deleteActiveNote()
            clearActiveNote()
            _events.tryEmit(EditorUiEvent.NOTE_DELETED)
        }
    }

    /**
     * Saves any pending text immediately, bypassing the debounce. The editor
     * calls this from onPause/onStop so process death cannot lose the last
     * keystrokes; the guard makes repeated calls no-ops.
     */
    @Suppress("TooGenericExceptionCaught")
    fun flushPendingText() {
        viewModelScope.launch {
            val current = _text.value
            if (current == lastSaved) return@launch
            try {
                repository.saveActiveNote(current)
                lastSaved = current
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Flush save failed")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun autosave(text: String) {
        if (text == lastSaved) return
        _isSaving.value = true
        try {
            repository.saveActiveNote(text)
            lastSaved = text
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Autosave failed; the next edit will retry")
        } finally {
            _isSaving.value = false
        }
    }

    private fun applyNote(note: Note?) {
        val previous = currentNote.value
        currentNote.value = note
        when {
            note == null ->
                if (previous != null) {
                    lastSaved = ""
                    setText("")
                }
            note.id != previous?.id ->
                if (restoredDraft) {
                    // The draft is newer than the persisted text; keep it.
                    restoredDraft = false
                } else if (note.text != lastSaved) {
                    lastSaved = note.text
                    setText(note.text)
                }
        }
        refreshCanArchiveOrDelete()
    }

    private fun clearActiveNote() {
        currentNote.value = null
        lastSaved = ""
        setText("")
    }

    private fun setText(text: String) {
        _text.value = text
        savedStateHandle[DRAFT_KEY] = text
        refreshCanArchiveOrDelete()
    }

    private fun refreshCanArchiveOrDelete() {
        val note = currentNote.value
        _canArchiveOrDelete.value = note != null && !note.copy(text = _text.value).isBlankNote
    }

    companion object {
        private const val AUTOSAVE_DEBOUNCE_MILLIS = 500L

        /**
         * Soft character limit driving [characterLimitProgress]'s ring fill.
         * Purely visual — typing past this is not blocked.
         */
        const val MAX_NOTE_LENGTH = 200

        /** [SavedStateHandle] key holding the in-progress draft. */
        const val DRAFT_KEY = "editor_draft"

        /** [ViewModelProvider.Factory] providing [EditorViewModel] with [repository]. */
        fun factory(repository: NotesRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    EditorViewModel(repository, createSavedStateHandle())
                }
            }
    }
}
