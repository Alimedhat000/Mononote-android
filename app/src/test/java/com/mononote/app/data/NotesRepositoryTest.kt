package com.mononote.app.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotesRepositoryTest {
    private val dao = mockk<NotesDao>(relaxed = true)
    private val settings = mockk<SettingsDataStore>(relaxed = true)
    private var clock = 1_000L

    private lateinit var repository: NotesRepository

    @BeforeEach
    fun setUp() {
        repository = NotesRepository(dao, settings) { clock }
    }

    @Test
    fun `getOrCreateActiveNote returns existing active note without creating`() =
        runTest {
            val existing = Note(id = 7, text = "existing", createdAt = 100, updatedAt = 200)
            coEvery { dao.getOrCreateBlankNote(clock) } returns existing

            assertEquals(existing, repository.getOrCreateActiveNote())

            coVerify(exactly = 1) { dao.getOrCreateBlankNote(clock) }
        }

    @Test
    fun `getOrCreateActiveNote creates a blank note when none exists`() =
        runTest {
            val created = Note(id = 42, text = "", createdAt = clock, updatedAt = clock)
            coEvery { dao.getOrCreateBlankNote(clock) } returns created

            assertEquals(created, repository.getOrCreateActiveNote())

            coVerify(exactly = 1) { dao.getOrCreateBlankNote(clock) }
        }

    @Test
    fun `saveActiveNote updates the active note text and timestamp in Room and the snapshot`() =
        runTest {
            val active = Note(id = 1, text = "old", createdAt = 100, updatedAt = 100)
            coEvery { dao.getActiveNote() } returns active
            clock = 500

            repository.saveActiveNote("new text")

            coVerify {
                dao.upsert(Note(id = 1, text = "new text", createdAt = 100, updatedAt = 500, archivedAt = null))
            }
            coVerify { settings.saveActiveNoteSnapshot("new text") }
        }

    @Test
    fun `saveActiveNote creates a note when none exists`() =
        runTest {
            coEvery { dao.getActiveNote() } returns null
            clock = 300
            coEvery { dao.getOrCreateBlankNote(any()) } returns
                Note(id = 42, text = "", createdAt = clock, updatedAt = clock)

            repository.saveActiveNote("hello")

            coVerify { dao.upsert(Note(id = 42, text = "hello", createdAt = 300, updatedAt = 300, archivedAt = null)) }
            coVerify { settings.saveActiveNoteSnapshot("hello") }
        }

    @Test
    fun `archiveActiveNote sets archivedAt and clears the snapshot`() =
        runTest {
            coEvery { dao.getActiveNote() } returns Note(id = 1, text = "note", createdAt = 100, updatedAt = 100)
            clock = 900

            repository.archiveActiveNote()

            coVerify { dao.setArchivedAt(1, 900) }
            coVerify { settings.saveActiveNoteSnapshot("") }
        }

    @Test
    fun `archiveActiveNote is a no-op when no active note exists`() =
        runTest {
            coEvery { dao.getActiveNote() } returns null

            repository.archiveActiveNote()

            coVerify(exactly = 0) { dao.setArchivedAt(any(), any()) }
            coVerify(exactly = 0) { settings.saveActiveNoteSnapshot(any()) }
        }

    @Test
    fun `deleteActiveNote hard deletes the row and clears the snapshot`() =
        runTest {
            coEvery { dao.getActiveNote() } returns Note(id = 3, text = "x", createdAt = 1, updatedAt = 2)

            repository.deleteActiveNote()

            coVerify { dao.deleteById(3) }
            coVerify { settings.saveActiveNoteSnapshot("") }
        }

    @Test
    fun `restoreNote archives active first and refreshes snapshot`() =
        runTest {
            coEvery { dao.getActiveNote() } returns Note(id = 1, text = "current", createdAt = 10, updatedAt = 20)
            coEvery { dao.getNoteById(2) } returns
                Note(id = 2, text = "restored", createdAt = 5, updatedAt = 6, archivedAt = 100)
            clock = 700

            val result = repository.restoreNote(2)

            assertEquals(true, result)
            coVerify { dao.archiveActiveThenRestore(1, 700, 2) }
            coVerify { settings.saveActiveNoteSnapshot("restored") }
        }

    @Test
    fun `restoreNote deletes blank active note and promotes the restored note`() =
        runTest {
            coEvery { dao.getActiveNote() } returns Note(id = 1, text = "", createdAt = 10, updatedAt = 20)
            coEvery { dao.getNoteById(2) } returns
                Note(id = 2, text = "restored", createdAt = 5, updatedAt = 6, archivedAt = 100)

            val result = repository.restoreNote(2)

            assertEquals(true, result)
            coVerify { dao.deleteActiveThenRestore(1, 2) }
            coVerify { settings.saveActiveNoteSnapshot("restored") }
        }

    @Test
    fun `restoreNote restores directly when no active note exists`() =
        runTest {
            coEvery { dao.getActiveNote() } returns null
            coEvery { dao.getNoteById(5) } returns
                Note(id = 5, text = "back", createdAt = 1, updatedAt = 2, archivedAt = 50)

            val result = repository.restoreNote(5)

            assertEquals(true, result)
            coVerify { dao.setArchivedAt(5, null) }
            coVerify { settings.saveActiveNoteSnapshot("back") }
        }

    @Test
    fun `restoreNote returns false without writing when the note does not exist`() =
        runTest {
            coEvery { dao.getNoteById(99) } returns null

            val result = repository.restoreNote(99)

            assertEquals(false, result)
            coVerify(exactly = 0) { dao.setArchivedAt(any(), any()) }
            coVerify(exactly = 0) { dao.deleteActiveThenRestore(any(), any()) }
            coVerify(exactly = 0) { dao.archiveActiveThenRestore(any(), any(), any()) }
            coVerify(exactly = 0) { settings.saveActiveNoteSnapshot(any()) }
        }

    @Test
    fun `observeArchivedNotes delegates to the DAO flow`() =
        runTest {
            val archived =
                listOf(
                    Note(id = 1, text = "old", createdAt = 10, updatedAt = 11, archivedAt = 100),
                    Note(id = 2, text = "older", createdAt = 9, updatedAt = 10, archivedAt = 200),
                )
            every { dao.observeArchivedNotes() } returns flowOf(archived)

            assertEquals(archived, repository.observeArchivedNotes().first())
        }

    @Test
    fun `deleteArchivedNote deletes the row without touching the widget snapshot`() =
        runTest {
            repository.deleteArchivedNote(9)

            coVerify { dao.deleteById(9) }
            coVerify(exactly = 0) { settings.saveActiveNoteSnapshot(any()) }
        }
}
