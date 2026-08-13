package com.mononote.app.ui.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mononote.app.data.Note
import com.mononote.app.data.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runEditorTest(block: suspend TestScope.() -> Unit) = runTest(mainDispatcher.scheduler) { block() }

    private fun note(
        text: String,
        id: Long = 1L,
    ) = Note(id = id, text = text, createdAt = 10, updatedAt = 10)

    private fun editorWith(
        repository: NotesRepository,
        activeNote: Note?,
    ): EditorViewModel {
        every { repository.observeActiveNote() } returns MutableStateFlow(activeNote)
        return EditorViewModel(repository, SavedStateHandle())
    }

    @Test
    fun `loads existing active note text`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = editorWith(repository, note("hello"))
            runCurrent()

            assertEquals("hello", vm.text.value)
        }

    @Test
    fun `loads blank text when no active note exists`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = editorWith(repository, null)
            runCurrent()

            assertEquals("", vm.text.value)
        }

    @Test
    fun `updateText reflects typing immediately without saving`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("typed")

            assertEquals("typed", vm.text.value)
            assertFalse(vm.isSaving.value)
            coVerify(exactly = 0) { repository.saveActiveNote(any()) }
        }

    @Test
    fun `autosaves once with the latest text after the debounce`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("h")
            vm.updateText("he")
            vm.updateText("hel")

            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS - 1)
            runCurrent()
            coVerify(exactly = 0) { repository.saveActiveNote(any()) }

            advanceTimeBy(1)
            runCurrent()
            coVerify(exactly = 1) { repository.saveActiveNote("hel") }
        }

    @Test
    fun `does not autosave when text is unchanged since the last save`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note("hello"))
            runCurrent()

            advanceTimeBy(1_000)
            runCurrent()

            coVerify(exactly = 0) { repository.saveActiveNote(any()) }
        }

    @Test
    fun `shows saving while the autosave is in flight`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote("slow") } coAnswers {
                delay(100)
                Unit
            }
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("slow")

            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS)
            runCurrent()
            assertTrue(vm.isSaving.value)

            advanceTimeBy(100)
            runCurrent()
            assertFalse(vm.isSaving.value)
        }

    @Test
    fun `archive archives the active note and emits a snackbar event`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note("hello"))
            runCurrent()

            vm.events.test {
                vm.archiveActiveNote()
                runCurrent()

                assertEquals(EditorUiEvent.NOTE_ARCHIVED, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) { repository.archiveActiveNote() }
        }

    @Test
    fun `delete deletes the active note and emits a snackbar event`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note("hello"))
            runCurrent()

            vm.events.test {
                vm.deleteActiveNote()
                runCurrent()

                assertEquals(EditorUiEvent.NOTE_DELETED, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) { repository.deleteActiveNote() }
        }

    @Test
    fun `archive clears the editor and hides the menu`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note("hello"))
            runCurrent()

            vm.archiveActiveNote()
            runCurrent()

            assertEquals("", vm.text.value)
            assertFalse(vm.canArchiveOrDelete.value)
        }

    @Test
    fun `canArchiveOrDelete is true for a non-blank active note`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = editorWith(repository, note("todo"))
            runCurrent()

            assertTrue(vm.canArchiveOrDelete.value)
        }

    @Test
    fun `canArchiveOrDelete is false for a blank or missing active note`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val blank = editorWith(repository, note("   "))
            runCurrent()
            val none = editorWith(repository, null)
            runCurrent()

            assertFalse(blank.canArchiveOrDelete.value)
            assertFalse(none.canArchiveOrDelete.value)
        }

    @Test
    fun `canArchiveOrDelete tracks in-progress text`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = editorWith(repository, note(""))
            runCurrent()

            assertFalse(vm.canArchiveOrDelete.value)

            vm.updateText("x")
            assertTrue(vm.canArchiveOrDelete.value)

            vm.updateText("")
            assertFalse(vm.canArchiveOrDelete.value)
        }

    @Test
    fun `flush saves pending text without waiting for the debounce`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("pending")
            vm.flushPendingText()
            runCurrent()

            coVerify(exactly = 1) { repository.saveActiveNote("pending") }
        }

    @Test
    fun `flush does not save when there is nothing pending`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note("saved"))
            runCurrent()

            vm.flushPendingText()
            runCurrent()

            coVerify(exactly = 0) { repository.saveActiveNote(any()) }
        }

    @Test
    fun `archive is a no-op when the active note is blank`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.archiveActiveNote()
            runCurrent()

            coVerify(exactly = 0) { repository.archiveActiveNote() }
        }

    @Test
    fun `delete is a no-op when the active note is blank`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.deleteActiveNote()
            runCurrent()

            coVerify(exactly = 0) { repository.deleteActiveNote() }
        }

    @Test
    fun `autosave keeps working after a failed save`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote("bad") } throws IllegalStateException("boom")
            coEvery { repository.saveActiveNote("good") } just runs
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("bad")
            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS)
            runCurrent()

            vm.updateText("good")
            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS)
            runCurrent()

            coVerify(exactly = 1) { repository.saveActiveNote("bad") }
            coVerify(exactly = 1) { repository.saveActiveNote("good") }
        }

    @Test
    fun `flush retries after a failed save`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            var attempts = 0
            coEvery { repository.saveActiveNote("pending") } coAnswers {
                attempts += 1
                if (attempts == 1) error("boom")
                Unit
            }
            val vm = editorWith(repository, note(""))
            runCurrent()

            vm.updateText("pending")
            vm.flushPendingText()
            runCurrent()
            vm.flushPendingText()
            runCurrent()

            coVerify(exactly = 2) { repository.saveActiveNote("pending") }
        }

    @Test
    fun `first autosave of a fresh note does not clobber in-progress text`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val notes = MutableStateFlow<Note?>(null)
            every { repository.observeActiveNote() } returns notes
            coEvery { repository.saveActiveNote(any()) } coAnswers {
                notes.value = note("", id = 1)
                testScheduler.runCurrent()
                notes.value = note("hi", id = 1)
            }
            val vm = EditorViewModel(repository, SavedStateHandle())
            runCurrent()

            vm.updateText("hi")
            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS)
            runCurrent()

            assertEquals("hi", vm.text.value)
        }

    @Test
    fun `loads the text of a newly created or restored active note`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val notes = MutableStateFlow(note("old", id = 1))
            every { repository.observeActiveNote() } returns notes
            val vm = EditorViewModel(repository, SavedStateHandle())
            runCurrent()

            notes.value = note("new", id = 2)
            runCurrent()

            assertEquals("new", vm.text.value)
        }

    @Test
    fun `updateText writes the draft to the saved state handle`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val savedStateHandle = SavedStateHandle()
            val vm = EditorViewModel(repository, savedStateHandle)
            runCurrent()

            vm.updateText("typed")

            assertEquals("typed", savedStateHandle[EditorViewModel.DRAFT_KEY])
        }

    @Test
    fun `restores a non-blank draft over the older persisted text`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val savedStateHandle = SavedStateHandle()
            savedStateHandle[EditorViewModel.DRAFT_KEY] = "newer draft"
            val notes = MutableStateFlow(note("older saved", id = 1))
            every { repository.observeActiveNote() } returns notes
            val vm = EditorViewModel(repository, savedStateHandle)
            runCurrent()

            assertEquals("newer draft", vm.text.value)
        }

    @Test
    fun `an empty draft does not block loading persisted text`() =
        runEditorTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.saveActiveNote(any()) } just runs
            val savedStateHandle = SavedStateHandle()
            savedStateHandle[EditorViewModel.DRAFT_KEY] = ""
            val notes = MutableStateFlow(note("saved", id = 1))
            every { repository.observeActiveNote() } returns notes
            val vm = EditorViewModel(repository, savedStateHandle)
            runCurrent()

            assertEquals("saved", vm.text.value)
        }

    private companion object {
        const val AUTOSAVE_DEBOUNCE_MILLIS = 500L
    }
}
