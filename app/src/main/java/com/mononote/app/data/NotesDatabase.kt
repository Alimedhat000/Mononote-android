package com.mononote.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for Mononote. Holds the single [Note] table; schema version 1.
 */
@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false,
)
abstract class NotesDatabase : RoomDatabase() {
    /** Returns the DAO used to query and mutate notes. */
    abstract fun notesDao(): NotesDao
}
