package com.mononote.app.ui.archive

import app.cash.turbine.test
import com.mononote.app.data.Note
import com.mononote.app.data.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runArchiveTest(block: suspend TestScope.() -> Unit) = runTest(mainDispatcher.scheduler) { block() }

    private fun archivedNote(
        text: String,
        id: Long,
    ) = Note(id = id, text = text, createdAt = 10, updatedAt = 10, archivedAt = 100)

    private fun activeNote(
        text: String,
        id: Long = 5,
    ) = Note(id = id, text = text, createdAt = 1, updatedAt = 2)

    private fun archiveWith(
        repository: NotesRepository,
        notes: List<Note>,
        activeNote: Note?,
    ): ArchiveViewModel {
        every { repository.observeArchivedNotes() } returns MutableStateFlow(notes)
        every { repository.observeActiveNote() } returns MutableStateFlow(activeNote)
        return ArchiveViewModel(repository)
    }

    @Test
    fun `loads archived notes`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1), archivedNote("b", 2)), null)
            runCurrent()

            val noteTexts = vm.uiState.value.notes
            assertEquals(listOf("a", "b"), noteTexts.map { it.text })
        }

    @Test
    fun `restore with no active note restores directly and navigates back`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.restoreNote(2) } returns true
            val vm = archiveWith(repository, listOf(archivedNote("b", 2)), null)
            runCurrent()

            vm.events.test {
                vm.onRestoreClick(archivedNote("b", 2))
                runCurrent()

                assertEquals(ArchiveEvent.NAVIGATE_BACK, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(vm.uiState.value.pendingRestore)
            coVerify(exactly = 1) { repository.restoreNote(2) }
        }

    @Test
    fun `restore with a blank active note restores directly and navigates back`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.restoreNote(1) } returns true
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), activeNote("   "))
            runCurrent()

            vm.events.test {
                vm.onRestoreClick(archivedNote("a", 1))
                runCurrent()

                assertEquals(ArchiveEvent.NAVIGATE_BACK, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(vm.uiState.value.pendingRestore)
            coVerify(exactly = 1) { repository.restoreNote(1) }
        }

    @Test
    fun `restore with a non-blank active note asks for confirmation first`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), activeNote("current"))
            runCurrent()

            vm.onRestoreClick(archivedNote("a", 1))
            runCurrent()

            val pendingRestore = vm.uiState.value.pendingRestore
            assertEquals(1L, pendingRestore?.id)
            coVerify(exactly = 0) { repository.restoreNote(any()) }
        }

    @Test
    fun `confirmed restore restores and navigates back`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.restoreNote(1) } returns true
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), activeNote("current"))
            runCurrent()
            vm.onRestoreClick(archivedNote("a", 1))
            runCurrent()

            vm.events.test {
                vm.onRestoreConfirm()
                runCurrent()

                assertEquals(ArchiveEvent.NAVIGATE_BACK, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(vm.uiState.value.pendingRestore)
            coVerify(exactly = 1) { repository.restoreNote(1) }
        }

    @Test
    fun `dismissing the restore dialog does not restore`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), activeNote("current"))
            runCurrent()
            vm.onRestoreClick(archivedNote("a", 1))
            runCurrent()

            vm.onRestoreDismiss()
            runCurrent()

            assertNull(vm.uiState.value.pendingRestore)
            coVerify(exactly = 0) { repository.restoreNote(any()) }
        }

    @Test
    fun `failed restore does not navigate back`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            coEvery { repository.restoreNote(1) } returns false
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), null)
            runCurrent()

            vm.events.test {
                vm.onRestoreClick(archivedNote("a", 1))
                runCurrent()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            coVerify(exactly = 1) { repository.restoreNote(1) }
        }

    @Test
    fun `delete asks for confirmation before deleting`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), null)
            runCurrent()

            vm.onDeleteClick(archivedNote("a", 1))
            runCurrent()

            val pendingDelete = vm.uiState.value.pendingDelete
            assertEquals(1L, pendingDelete?.id)
            coVerify(exactly = 0) { repository.deleteArchivedNote(any()) }
        }

    @Test
    fun `confirmed delete deletes and emits a snackbar event`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), null)
            runCurrent()
            vm.onDeleteClick(archivedNote("a", 1))
            runCurrent()

            vm.events.test {
                vm.onDeleteConfirm()
                runCurrent()

                assertEquals(ArchiveEvent.NOTE_DELETED, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(vm.uiState.value.pendingDelete)
            coVerify(exactly = 1) { repository.deleteArchivedNote(1) }
        }

    @Test
    fun `dismissing the delete dialog does not delete`() =
        runArchiveTest {
            val repository = mockk<NotesRepository>(relaxed = true)
            val vm = archiveWith(repository, listOf(archivedNote("a", 1)), null)
            runCurrent()
            vm.onDeleteClick(archivedNote("a", 1))
            runCurrent()

            vm.onDeleteDismiss()
            runCurrent()

            assertNull(vm.uiState.value.pendingDelete)
            coVerify(exactly = 0) { repository.deleteArchivedNote(any()) }
        }
}
