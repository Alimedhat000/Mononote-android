package com.mononote.app.widget

import com.mononote.app.data.SettingsDataStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MononoteWidgetSyncTest {
    @Test
    fun `refreshes widget on every snapshot change`() =
        runTest {
            val settings = mockk<SettingsDataStore>()
            every { settings.activeNoteSnapshot } returns flowOf("hello", "world")
            val refreshed = mutableListOf<String>()
            val sync = MononoteWidgetSync(settings) { text -> refreshed += text }

            sync.start(this)
            advanceUntilIdle()

            assertEquals(listOf("hello", "world"), refreshed)
        }
}
