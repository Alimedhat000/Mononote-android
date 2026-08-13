package com.mononote.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room data access object for notes.
 *
 * An active note is one with a null [Note.archivedAt]. Queries that filter on
 * that column use a `LIMIT 1` because the repository guarantees at most one
 * active row.
 */
@Dao
interface NotesDao {
    /** Returns the active note, or null when none exists. */
    @Query("SELECT * FROM notes WHERE archivedAt IS NULL LIMIT 1")
    suspend fun getActiveNote(): Note?

    /** Emits the active note, or null when none exists, on every change. */
    @Query("SELECT * FROM notes WHERE archivedAt IS NULL LIMIT 1")
    fun observeActiveNote(): Flow<Note?>

    /** Returns the note with the given row id, or null. */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    /** Returns all archived notes, newest archived first. */
    @Query("SELECT * FROM notes WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    suspend fun getArchivedNotes(): List<Note>

    /** Emits all archived notes, newest archived first, on every change. */
    @Query("SELECT * FROM notes WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun observeArchivedNotes(): Flow<List<Note>>

    /** Counts active notes; used to assert the single-active-note invariant. */
    @Query("SELECT COUNT(*) FROM notes WHERE archivedAt IS NULL")
    suspend fun countActiveNotes(): Int

    /** Inserts a new note or updates an existing one by id. */
    @Upsert
    suspend fun upsert(note: Note): Long

    /** Sets [archivedAt] on the note with [id]; null un-archives it. */
    @Query("UPDATE notes SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun setArchivedAt(
        id: Long,
        archivedAt: Long?,
    )

    /** Permanently deletes the note with the given row id. */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Atomically archives the current active note (when [archiveActiveId] is
     * not null) and activates [restoreId], preserving the invariant that at
     * most one note is active.
     */
    @Transaction
    suspend fun archiveActiveThenRestore(
        archiveActiveId: Long?,
        archivedAt: Long,
        restoreId: Long,
    ) {
        if (archiveActiveId != null) {
            setArchivedAt(archiveActiveId, archivedAt)
        }
        setArchivedAt(restoreId, null)
    }

    /**
     * Returns the active note, creating a blank one with [createdAt] when none
     * exists. Runs atomically: the read, the invariant assertion, and the
     * insert share one transaction, so concurrent first access can never
     * produce two active rows.
     */
    @Transaction
    suspend fun getOrCreateBlankNote(createdAt: Long): Note {
        val existing = getActiveNote()
        if (existing != null) {
            return existing
        }
        check(countActiveNotes() == 0) { "Single-active-note invariant violated" }
        val id = upsert(Note(text = "", createdAt = createdAt, updatedAt = createdAt))
        return Note(id = id, text = "", createdAt = createdAt, updatedAt = createdAt)
    }

    /**
     * Atomically deletes the blank active note [deleteId] and activates
     * [restoreId], used when restoring over an empty active note.
     */
    @Transaction
    suspend fun deleteActiveThenRestore(
        deleteId: Long,
        restoreId: Long,
    ) {
        deleteById(deleteId)
        setArchivedAt(restoreId, null)
    }
}
