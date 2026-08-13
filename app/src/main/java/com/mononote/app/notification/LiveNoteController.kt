package com.mononote.app.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped holder of the go-live state: whether the live-note foreground
 * service is currently running and showing the note as a persistent
 * notification.
 *
 * The controller is deliberately context-free: it only mirrors what
 * [LiveNoteService] reports through [onLiveStarted]/[onLiveStopped], so the
 * UI can reflect the toggle state and tests can exercise it without Android
 * dependencies. The screen starts and stops the service itself.
 */
class LiveNoteController {
    private val _isLive = MutableStateFlow(false)

    /** True while the live-note foreground service is running. */
    val isLive: StateFlow<Boolean> = _isLive.asStateFlow()

    /** Marks the service as running; called from [LiveNoteService]. */
    fun onLiveStarted() {
        _isLive.value = true
    }

    /** Marks the service as stopped; called from [LiveNoteService]. */
    fun onLiveStopped() {
        _isLive.value = false
    }
}
