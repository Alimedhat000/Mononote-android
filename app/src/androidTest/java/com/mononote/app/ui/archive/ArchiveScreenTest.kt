package com.mononote.app.ui.archive

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mononote.app.MononoteApp
import com.mononote.app.ui.theme.MononoteTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchiveScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val app
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as MononoteApp

    /**
     * AndroidJUnitRunner does not clear app data between runs, so leftover
     * active and archived notes from a previous run would make list and
     * empty-state assertions non-deterministic. Purge both.
     */
    @Before
    fun resetNotes() {
        runBlocking {
            app.repository.deleteActiveNote()
            app.repository.getArchivedNotes().forEach { app.repository.deleteArchivedNote(it.id) }
        }
    }

    private fun setArchiveContent() {
        composeRule.setContent {
            MononoteTheme {
                ArchiveScreen(
                    repository = app.repository,
                    onBack = {},
                )
            }
        }
    }

    private fun archiveNote(text: String) {
        runBlocking {
            app.repository.saveActiveNote(text)
            app.repository.archiveActiveNote()
        }
    }

    private fun waitUntilGone(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun showsEmptyStateWhenNothingArchived() {
        setArchiveContent()

        composeRule.onNodeWithText("Archived Notes").assertIsDisplayed()
        composeRule.onNodeWithText("No archived notes yet").assertIsDisplayed()
    }

    @Test
    fun showsArchivedNotesWithPerRowActions() {
        archiveNote("archived note")
        setArchiveContent()

        composeRule.onNodeWithText("archived note").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Restore").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete note").assertIsDisplayed()
    }

    @Test
    fun restoreWithNoActiveNoteMovesNoteOutOfArchive() {
        archiveNote("archived note")
        setArchiveContent()

        composeRule.onNodeWithContentDescription("Restore").performClick()

        waitUntilGone("archived note")
        composeRule.onNodeWithText("No archived notes yet").assertIsDisplayed()
    }

    @Test
    fun restoreOverNonBlankActiveNoteAsksForConfirmation() {
        archiveNote("archived note")
        runBlocking { app.repository.saveActiveNote("current note") }
        setArchiveContent()

        composeRule.onNodeWithContentDescription("Restore").performClick()
        composeRule.onNodeWithText("Restore note?").assertIsDisplayed()

        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Restore note?").assertDoesNotExist()
        composeRule.onNodeWithText("archived note").assertIsDisplayed()
    }

    @Test
    fun confirmedRestoreRemovesNoteFromArchive() {
        archiveNote("archived note")
        runBlocking { app.repository.saveActiveNote("current note") }
        setArchiveContent()

        composeRule.onNodeWithContentDescription("Restore").performClick()
        composeRule.onNodeWithText("Restore").performClick()

        waitUntilGone("archived note")
        composeRule.onNodeWithText("No archived notes yet").assertIsDisplayed()
    }

    @Test
    fun deleteRequiresConfirmationAndRemovesNote() {
        archiveNote("archived note")
        setArchiveContent()

        composeRule.onNodeWithContentDescription("Delete note").performClick()
        composeRule.onNodeWithText("Delete note?").assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()

        waitUntilGone("archived note")
        composeRule.onNodeWithText("No archived notes yet").assertIsDisplayed()
    }

    @Test
    fun cancelKeepsArchivedNote() {
        archiveNote("archived note")
        setArchiveContent()

        composeRule.onNodeWithContentDescription("Delete note").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.onNodeWithText("Delete note?").assertDoesNotExist()
        composeRule.onNodeWithText("archived note").assertIsDisplayed()
    }
}
