package com.mononote.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single note.
 *
 * Mononote holds at most one active note at a time: a note is active while
 * [archivedAt] is null. Timestamps are epoch millis.
 *
 * @property id Auto-generated row id; 0 until the note is inserted.
 * @property text The note body.
 * @property createdAt When the note was first created.
 * @property updatedAt When the note text was last saved.
 * @property archivedAt When the note was archived, or null while active.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
)

/**
 * Whether the note holds no meaningful content (empty or whitespace-only).
 *
 * Single source of truth for the "blank note" definition: the editor's
 * overflow menu (Phase 3) and the restore flow (Phase 4) must use this so the
 * blank checks never diverge.
 */
val Note.isBlankNote: Boolean
    get() = text.isBlank()
