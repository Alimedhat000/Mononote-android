package com.mononote.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for notes, coordinating Room and DataStore.
 *
 * Enforces the single-active-note invariant: at most one note with a null
 * [Note.archivedAt] exists at any time. The read-and-create path runs inside
 * one transaction and asserts the invariant before inserting, so a second
 * active note can never appear. [restoreNote] archives a non-blank active
 * note, or deletes a blank one, before activating the restored note.
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
     * Returns the active note, creating a blank one when none exists. The
     * read-and-create happens in one transaction that asserts the
     * single-active-note invariant before inserting.
     */
    suspend fun getOrCreateActiveNote(): Note = dao.getOrCreateBlankNote(clock())

    /**
     * Saves [text] as the active note, creating it on first use, and updates
     * the widget snapshot in the same suspend call. The create path goes
     * through the transactional [NotesDao.getOrCreateBlankNote], so a first
     * write can never race into a second active row.
     */
    suspend fun saveActiveNote(text: String) {
        val now = clock()
        val active = dao.getActiveNote()
        if (active == null) {
            val created = dao.getOrCreateBlankNote(now)
            dao.upsert(created.copy(text = text, updatedAt = now))
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
     * Restores [noteId] as the active note, returning true on success.
     *
     * A blank active note is deleted and the restored note promoted; a
     * non-blank active note is archived first. Both transitions are
     * transactional. Returns false without writing anything when no note with
     * [noteId] exists, so callers can report the failure instead of silently
     * blanking the widget snapshot.
     */
    suspend fun restoreNote(noteId: Long): Boolean {
        val restored = dao.getNoteById(noteId) ?: return false
        val active = dao.getActiveNote()
        when {
            active == null -> dao.setArchivedAt(noteId, null)
            active.isBlankNote -> dao.deleteActiveThenRestore(active.id, noteId)
            else -> dao.archiveActiveThenRestore(active.id, clock(), noteId)
        }
        settingsDataStore.saveActiveNoteSnapshot(restored.text)
        return true
    }
}
