package com.mononote.app.ui.editor

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    /**
     * AndroidJUnitRunner does not clear app data between runs, so a leftover
     * active note from a previous run could hide the blank-note placeholder.
     * Reset to a clean slate so the assertions are deterministic.
     */
    @Before
    fun resetActiveNote() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as MononoteApp
        runBlocking { app.repository.deleteActiveNote() }
    }

    @Test
    fun showsEditorShell() {
        composeRule.setContent {
            MononoteTheme {
                val app = LocalContext.current.applicationContext as MononoteApp
                EditorScreen(repository = app.repository)
            }
        }

        composeRule.onNodeWithText("Mononote").assertIsDisplayed()
        composeRule.onNodeWithText("Start typing...").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
    }
}
