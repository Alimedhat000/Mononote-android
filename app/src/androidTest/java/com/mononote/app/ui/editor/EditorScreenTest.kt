package com.mononote.app.ui.editor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mononote.app.ui.theme.MononoteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsAppName() {
        composeRule.setContent {
            MononoteTheme {
                EditorScreen(onOpenArchive = {})
            }
        }

        composeRule.onNodeWithText("Mononote").assertIsDisplayed()
    }
}
