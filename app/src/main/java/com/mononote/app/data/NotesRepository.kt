package com.mononote.app.data

import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val dao: NotesDao,
    private val settingsDataStore: SettingsDataStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeActiveNote(): Flow<Note?> = dao.observeActiveNote()

    suspend fun getOrCreateActiveNote(): Note = dao.getActiveNote() ?: createBlankNote()

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

    suspend fun archiveActiveNote() {
        val active = dao.getActiveNote() ?: return
        dao.setArchivedAt(active.id, clock())
        settingsDataStore.saveActiveNoteSnapshot("")
    }

    suspend fun deleteActiveNote() {
        val active = dao.getActiveNote() ?: return
        dao.deleteById(active.id)
        settingsDataStore.saveActiveNoteSnapshot("")
    }

    suspend fun getArchivedNotes(): List<Note> = dao.getArchivedNotes()

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
