package com.mononote.app.ui.editor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
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
class EditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val app
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as MononoteApp

    /**
     * AndroidJUnitRunner does not clear app data between runs, so a leftover
     * active note from a previous run could hide the blank-note placeholder.
     * Reset to a clean slate so the assertions are deterministic.
     */
    @Before
    fun resetActiveNote() {
        runBlocking { app.repository.deleteActiveNote() }
    }

    private fun setEditorContent() {
        composeRule.setContent {
            MononoteTheme {
                EditorScreen(
                    repository = app.repository,
                    onOpenArchive = {},
                    liveNoteController = app.liveNoteController,
                )
            }
        }
    }

    @Test
    fun showsEditorShell() {
        setEditorContent()
        composeRule.onNodeWithText("Mononote").assertIsDisplayed()
        composeRule.onNodeWithText("Start typing...").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
    }

    @Test
    fun hidesGoLiveArchiveAndDeleteWhenNoteBlank() {
        setEditorContent()
        composeRule.onNodeWithContentDescription("Go live").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Archive note").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Delete note").assertDoesNotExist()
    }

    @Test
    fun showsDonePillWhileEditing() {
        setEditorContent()
        composeRule.onNode(hasSetTextAction()).performClick()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Go live").assertDoesNotExist()
    }

    @Test
    fun swapsDoneForActionBarWhenFocusLost() {
        runBlocking { app.repository.saveActiveNote("hello world") }
        setEditorContent()
        composeRule.onNode(hasSetTextAction()).performClick()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
        composeRule.onNodeWithText("Done").performClick()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Go live").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete note").assertIsDisplayed()
    }

    @Test
    fun showsGoLiveArchiveAndDeleteWhenNoteNotEmpty() {
        runBlocking { app.repository.saveActiveNote("hello world") }
        setEditorContent()
        composeRule.onNodeWithContentDescription("Go live").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Archive note").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete note").assertIsDisplayed()
    }

    @Test
    fun deleteOpensConfirmDialog() {
        runBlocking { app.repository.saveActiveNote("hello world") }
        setEditorContent()
        composeRule.onNodeWithContentDescription("Delete note").performClick()
        composeRule.onNodeWithText("Delete note?").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Delete note?").assertDoesNotExist()
    }

    @Test
    fun overflowMenuOffersDeleteAndArchivedNotesWhenNotEmpty() {
        runBlocking { app.repository.saveActiveNote("hello world") }
        setEditorContent()
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete note").assertIsDisplayed()
        composeRule.onNodeWithText("View archived notes").assertIsDisplayed()
    }

    @Test
    fun overflowMenuHidesDeleteWhenBlank() {
        setEditorContent()
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete note").assertDoesNotExist()
        composeRule.onNodeWithText("View archived notes").assertIsDisplayed()
    }
}
