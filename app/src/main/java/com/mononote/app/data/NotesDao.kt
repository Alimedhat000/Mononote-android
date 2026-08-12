package com.mononote.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE archivedAt IS NULL LIMIT 1")
    suspend fun getActiveNote(): Note?

    @Query("SELECT * FROM notes WHERE archivedAt IS NULL LIMIT 1")
    fun observeActiveNote(): Flow<Note?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    suspend fun getArchivedNotes(): List<Note>

    @Query("SELECT COUNT(*) FROM notes WHERE archivedAt IS NULL")
    suspend fun countActiveNotes(): Int

    @Upsert
    suspend fun upsert(note: Note): Long

    @Query("UPDATE notes SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun setArchivedAt(
        id: Long,
        archivedAt: Long?,
    )

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

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
}
