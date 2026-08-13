package com.mononote.app.notification

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.mononote.app.MononoteApp
import com.mononote.app.data.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service behind "Go live": keeps the active note visible as a
 * persistent live notification that stays in sync as the note is edited.
 *
 * Started with the current editor text in [EXTRA_NOTE_TEXT], it posts a
 * foreground notification with that text and observes the repository so every
 * autosave refreshes it. Because the editor's autosave is debounced, the
 * repository may still hold the previous text (or no row at all) when the
 * service starts; the observer keeps the start text until a real note arrives
 * instead of stopping on that transient state. The service only stops itself
 * when a note that was already shown disappears (archived or deleted). On
 * process death Android removes the foreground notification, so a stale note
 * can never linger.
 */
class LiveNoteService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observationJob: Job? = null

    /** True once the observer has shown a non-blank note. */
    private var shownNote = false

    private val app: MononoteApp get() = applicationContext as MononoteApp
    private val controller: LiveNoteController get() = app.liveNoteController
    private val repository: NotesRepository get() = app.repository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        LiveNoteNotifications.createChannel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopLive()
            return START_NOT_STICKY
        }
        val initialText = intent?.getStringExtra(EXTRA_NOTE_TEXT).orEmpty()
        val notification = LiveNoteNotifications.build(this, initialText)
        ServiceCompat.startForeground(
            this,
            LiveNoteNotifications.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        controller.onLiveStarted()
        observeActiveNote()
        return START_NOT_STICKY
    }

    private fun observeActiveNote() {
        if (observationJob != null) return
        observationJob =
            serviceScope.launch {
                repository.observeActiveNote().collect { note ->
                    when {
                        note != null && note.text.isNotBlank() -> {
                            shownNote = true
                            LiveNoteNotifications.notify(this@LiveNoteService, note.text)
                        }
                        note == null && shownNote -> stopLive()
                        note == null -> Unit
                        else -> LiveNoteNotifications.notify(this@LiveNoteService, note.text)
                    }
                }
            }
    }

    private fun stopLive() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        observationJob?.cancel()
        serviceScope.cancel()
        controller.onLiveStopped()
        super.onDestroy()
    }

    companion object {
        /** Action sent to stop the live note, from the notification Stop button. */
        const val ACTION_STOP = "com.mononote.app.action.STOP_LIVE"

        /** Extra carrying the editor's current text when going live. */
        const val EXTRA_NOTE_TEXT = "com.mononote.app.extra.NOTE_TEXT"
    }
}
