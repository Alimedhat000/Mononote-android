package com.mononote.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for notes, coordinating Room and DataStore.
 *
 * Enforces the single-active-note invariant: at most one note with a null
 * [Note.archivedAt] exists at any time. Reads reuse the existing active note
 * rather than inserting a second row, and [restoreNote] archives the current
 * active note (transactionally) before activating the restored one.
 *
 * Every save of the active note text also updates the widget snapshot in the
 * same suspend call, so the home-screen widget never shows stale text.
 *
 * @param dao Room data access object for notes.
 * @param settingsDataStore DataStore-backed widget snapshot.
 * @param clock Supplies epoch millis for timestamps; injectable for tests.
 */
class NotesRepository(
    private val dao: NotesDao,
    private val settingsDataStore: SettingsDataStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /** Emits the current active note, or null when none exists. */
    fun observeActiveNote(): Flow<Note?> = dao.observeActiveNote()

    /**
     * Returns the active note, creating a blank one when none exists.
     * Never creates a second active note.
     */
    suspend fun getOrCreateActiveNote(): Note = dao.getActiveNote() ?: createBlankNote()

    /**
     * Saves [text] as the active note, creating it on first use, and updates
     * the widget snapshot in the same suspend call.
     */
    suspend fun saveActiveNote(text: String) {
        val now = clock()
        val active = dao.getActiveNote()
        if (active == null) {
            dao.upsert(Note(text = text, createdAt = now, updatedAt = now))
        } else {
            dao.upsert(active.copy(text = text, updatedAt = now))
        }
        settingsDataStore.saveActiveNoteSnapshot(text)
    }

    /**
     * Archives the active note (reversible, no confirmation) and clears the
     * widget snapshot. No-op when no active note exists.
     */
    suspend fun archiveActiveNote() {
        val active = dao.getActiveNote() ?: return
        dao.setArchivedAt(active.id, clock())
        settingsDataStore.saveActiveNoteSnapshot("")
    }

    /**
     * Permanently deletes the active note row and clears the widget snapshot.
     * No-op when no active note exists.
     */
    suspend fun deleteActiveNote() {
        val active = dao.getActiveNote() ?: return
        dao.deleteById(active.id)
        settingsDataStore.saveActiveNoteSnapshot("")
    }

    /** Returns all archived notes, newest archived first. */
    suspend fun getArchivedNotes(): List<Note> = dao.getArchivedNotes()

    /**
     * Restores [noteId] as the active note. The current active note is
     * archived first in one transaction, then the widget snapshot is refreshed
     * with the restored note's text.
     */
    suspend fun restoreNote(noteId: Long) {
        val active = dao.getActiveNote()
        dao.archiveActiveThenRestore(active?.id, clock(), noteId)
        val restored = dao.getNoteById(noteId)
        settingsDataStore.saveActiveNoteSnapshot(restored?.text.orEmpty())
    }

    private suspend fun createBlankNote(): Note {
        val now = clock()
        val note = Note(text = "", createdAt = now, updatedAt = now)
        val id = dao.upsert(note)
        return note.copy(id = id)
    }
}
