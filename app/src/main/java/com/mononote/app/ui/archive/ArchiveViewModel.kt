package com.mononote.app.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mononote.app.data.Note
import com.mononote.app.data.NotesRepository
import com.mononote.app.data.isBlankNote
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** One-shot events the archive screen surfaces. */
enum class ArchiveEvent {
    /** An archived note was permanently deleted; show a snackbar. */
    NOTE_DELETED,

    /** A note was restored; return to the editor. */
    NAVIGATE_BACK,
}

/**
 * UI state for the archive screen.
 *
 * @property notes All archived notes, newest archived first.
 * @property pendingDelete The note awaiting permanent-delete confirmation.
 * @property pendingRestore The note awaiting restore confirmation.
 */
data class ArchiveUiState(
    val notes: List<Note> = emptyList(),
    val pendingDelete: Note? = null,
    val pendingRestore: Note? = null,
)

/**
 * Drives the archive screen: observes the archived-note list and the active
 * note, and coordinates restore/delete with the repository.
 *
 * Restore follows the app's product rule: an empty or missing active note is
 * swapped out directly, while a non-blank active note first asks for
 * confirmation, because restoring would archive it. The repository's
 * [NotesRepository.restoreNote] handles both transitions; a failed restore
 * (the note vanished) is logged and never navigates.
 *
 * Delete is permanent, so it always asks for confirmation first. Deleting an
 * archived note must not touch the widget snapshot, which mirrors only the
 * active note.
 *
 * @param repository Single source of truth for note data.
 */
class ArchiveViewModel(
    private val repository: NotesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    private val activeNote = MutableStateFlow<Note?>(null)
    private val _events = MutableSharedFlow<ArchiveEvent>(extraBufferCapacity = 1)

    /** The archived-note list and any dialog awaiting confirmation. */
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    /** One-shot events (delete snackbar, navigate back after restore). */
    val events: SharedFlow<ArchiveEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeArchivedNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
        viewModelScope.launch {
            repository.observeActiveNote().collect { activeNote.value = it }
        }
    }

    /**
     * Starts the restore flow for [note]. When the active note is empty or
     * missing, restores immediately; otherwise shows a confirmation dialog
     * because the current note will be archived to make room.
     */
    fun onRestoreClick(note: Note) {
        val active = activeNote.value
        when {
            active == null || active.isBlankNote -> restore(note)
            else -> _uiState.update { it.copy(pendingRestore = note) }
        }
    }

    /** Confirms a pending restore and activates the note. */
    fun onRestoreConfirm() {
        _uiState.value.pendingRestore?.let(::restore)
    }

    /** Dismisses the restore-confirmation dialog. */
    fun onRestoreDismiss() {
        _uiState.update { it.copy(pendingRestore = null) }
    }

    /** Shows the permanent-delete confirmation dialog for [note]. */
    fun onDeleteClick(note: Note) {
        _uiState.update { it.copy(pendingDelete = note) }
    }

    /** Confirms a pending delete and permanently removes the note. */
    fun onDeleteConfirm() {
        val note = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            repository.deleteArchivedNote(note.id)
            _uiState.update { it.copy(pendingDelete = null) }
            _events.tryEmit(ArchiveEvent.NOTE_DELETED)
        }
    }

    /** Dismisses the delete-confirmation dialog. */
    fun onDeleteDismiss() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    private fun restore(note: Note) {
        viewModelScope.launch {
            val restored = repository.restoreNote(note.id)
            _uiState.update { it.copy(pendingRestore = null) }
            if (restored) {
                _events.tryEmit(ArchiveEvent.NAVIGATE_BACK)
            } else {
                Timber.w("Restore failed: no archived note with id ${note.id}")
            }
        }
    }

    companion object {
        /** [ViewModelProvider.Factory] providing [ArchiveViewModel] with [repository]. */
        fun factory(repository: NotesRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ArchiveViewModel(repository)
                }
            }
    }
}
