package com.mononote.app.notification

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LiveNoteControllerTest {
    @Test
    fun isOffByDefault() {
        val controller = LiveNoteController()
        assertFalse(controller.isLive.value)
    }

    @Test
    fun startedMarksLive() {
        val controller = LiveNoteController()
        controller.onLiveStarted()
        assertTrue(controller.isLive.value)
    }

    @Test
    fun stoppedAfterStartMarksOff() {
        val controller = LiveNoteController()
        controller.onLiveStarted()
        controller.onLiveStopped()
        assertFalse(controller.isLive.value)
    }

    @Test
    fun emitsOnlyDistinctTransitions() =
        runTest {
            val controller = LiveNoteController()
            controller.isLive.test {
                assertEquals(false, awaitItem())
                controller.onLiveStarted()
                assertEquals(true, awaitItem())
                controller.onLiveStarted()
                controller.onLiveStopped()
                assertEquals(false, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
